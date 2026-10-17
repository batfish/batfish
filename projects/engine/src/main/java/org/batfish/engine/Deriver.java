package org.batfish.engine;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.engine.SnapshotRequest.Device;
import org.batfish.identifiers.SnapshotId;

/**
 * Derives a proposed snapshot by replacing changed devices with complete native candidate configs.
 *
 * <p>Legacy command deltas append before a trailing {@code end}; this is supported only for
 * explicitly validated operations, not as a general device CLI interpreter. All inputs are parsed
 * into a new snapshot. Anything newly unmodeled on a changed device makes its verdict unknown.
 */
public final class Deriver {

  /** A complete candidate config, or legacy CLI lines for a validated operation. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record Change(
      @Nonnull String device, @Nullable List<String> lines, @Nullable String configText) {
    public Change {
      if ((lines == null) == (configText == null)) {
        throw new IllegalArgumentException("provide exactly one of lines or configText");
      }
      if (lines != null) {
        lines = ImmutableList.copyOf(lines);
        if (lines.isEmpty()) throw new IllegalArgumentException("lines must not be empty");
      }
      if (configText != null && configText.isBlank()) {
        throw new IllegalArgumentException("configText must not be empty");
      }
    }

    public Change(String device, List<String> lines) {
      this(device, lines, null);
    }
  }

  /**
   * The derived snapshot, and the holes the change introduced. A non-empty {@code introduced} means
   * the engine cannot vouch for the change's effect.
   */
  public record Derived(
      @Nonnull SnapshotBuilder.Built snapshot, @Nonnull List<CoverageHole> introduced) {}

  private final SnapshotBuilder _builder;

  public Deriver(@Nonnull SnapshotBuilder builder) {
    _builder = builder;
  }

  public @Nonnull Derived derive(
      @Nonnull SnapshotBuilder.Built base, @Nonnull List<Change> changes) {
    return derive(base, changes, EngineBatfish.freshSnapshotId("snap"));
  }

  Derived derive(SnapshotBuilder.Built base, List<Change> changes, SnapshotId id) {
    Map<String, List<String>> linesByDevice =
        changes.stream()
            .filter(c -> c.lines() != null)
            .collect(
                Collectors.groupingBy(
                    c -> c.device().toLowerCase(Locale.ROOT),
                    Collectors.flatMapping(c -> c.lines().stream(), Collectors.toList())));
    Map<String, String> replacements = new java.util.HashMap<>();
    for (Change change : changes) {
      if (change.configText() == null) continue;
      String node = change.device().toLowerCase(Locale.ROOT);
      if (linesByDevice.containsKey(node)
          || replacements.putIfAbsent(node, change.configText()) != null) {
        throw new IllegalArgumentException("ambiguous candidate for " + node);
      }
    }
    Set<String> changed = new HashSet<>(linesByDevice.keySet());
    changed.addAll(replacements.keySet());
    Set<String> known =
        base.request().devices().stream()
            .map(d -> d.name().toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());
    for (String device : changed) {
      if (!known.contains(device)) {
        throw new IllegalArgumentException("change for unknown device " + device);
      }
    }

    List<Device> devices = new ArrayList<>();
    for (Device d : base.request().devices()) {
      String node = d.name().toLowerCase(Locale.ROOT);
      List<String> lines = linesByDevice.get(node);
      String replacement = replacements.get(node);
      devices.add(
          lines == null && replacement == null
              ? d
              : new Device(
                  d.name(),
                  d.dialect(),
                  replacement != null ? replacement : apply(d.configText(), lines),
                  d.linuxInterfaces()));
    }
    SnapshotRequest proposed =
        new SnapshotRequest(devices, base.request().layer1(), base.request().environment());
    SnapshotBuilder.Built built = _builder.build(id, proposed);
    List<CoverageHole> introduced =
        new ArrayList<>(introduced(base.holes(), built.holes(), changed));
    linesByDevice.forEach(
        (device, lines) -> introduced.addAll(redeclaredClauses(device, base, lines)));
    return new Derived(built, ImmutableList.copyOf(introduced));
  }

  private static final Pattern ROUTE_MAP_CLAUSE =
      Pattern.compile("^\\s*route-map\\s+(\\S+)\\s+(permit|deny)\\s+(\\d+)\\s*$");
  private static final Pattern NO_ROUTE_MAP =
      Pattern.compile("^\\s*no\\s+route-map\\s+(\\S+)(?:\\s+(?:permit|deny)\\s+(\\d+))?\\s*$");

  /**
   * Re-declaring an existing route-map clause with the other action is silent in Batfish, which
   * keeps the original, while devices replace the clause (FRR) or change its action in place. No
   * warning reveals it, so the deriver flags it: the change's effect is unknown.
   */
  private static List<CoverageHole> redeclaredClauses(
      String device, SnapshotBuilder.Built base, List<String> lines) {
    DeviceFacts facts = base.facts().get(device);
    if (facts == null) {
      return ImmutableList.of();
    }
    ImmutableList.Builder<CoverageHole> holes = ImmutableList.builder();
    Map<String, String> actions = new HashMap<>();
    for (DeviceFacts.PolicyFacts policy : facts.policies()) {
      for (DeviceFacts.ClauseFacts clause : policy.clauses()) {
        if (clause.lines().isEmpty()) continue;
        Matcher header = ROUTE_MAP_CLAUSE.matcher(clause.lines().get(0));
        if (header.matches()) actions.put(clause.name(), header.group(2));
      }
    }
    boolean frr =
        base.request().devices().stream()
            .anyMatch(d -> d.name().equalsIgnoreCase(device) && d.dialect() == Dialect.FRR_LINUX);
    // The parser sees physical lines even when an agent sends a multiline CLI
    // block in one array element. Coverage checks must inspect the same input.
    for (String line : lines.stream().flatMap(String::lines).toList()) {
      Matcher removal = NO_ROUTE_MAP.matcher(line);
      if (frr && removal.matches()) {
        if (removal.group(2) == null) {
          actions.keySet().removeIf(clause -> clause.startsWith(removal.group(1) + " "));
        } else {
          actions.remove(removal.group(1) + " " + removal.group(2));
        }
        continue;
      }
      Matcher m = ROUTE_MAP_CLAUSE.matcher(line);
      if (!m.matches()) {
        continue;
      }
      String clause = m.group(1) + " " + m.group(3);
      String previous = actions.putIfAbsent(clause, m.group(2));
      if (previous != null && !previous.equals(m.group(2))) {
        holes.add(
            new CoverageHole(
                device,
                CoverageHole.POLICY,
                "redeclared-clause",
                null,
                line.strip()
                    + " changes the action of an existing clause; remove the clause"
                    + " first"));
      }
    }
    return holes.build();
  }

  /** Inserts {@code lines} after the config and before a trailing {@code end}, if any. */
  static @Nonnull String apply(@Nonnull String config, @Nonnull List<String> lines) {
    String body = config.endsWith("\n") ? config : config + "\n";
    String tail = "";
    int lastLineStart = body.lastIndexOf('\n', body.length() - 2) + 1;
    if (body.substring(lastLineStart).strip().equals("end")) {
      tail = body.substring(lastLineStart);
      body = body.substring(0, lastLineStart);
    }
    StringBuilder sb = new StringBuilder(body);
    for (String line : lines) {
      sb.append(line).append('\n');
    }
    return sb.append(tail).toString();
  }

  /** Holes on changed devices that the base did not have, ignoring line numbers. */
  private static List<CoverageHole> introduced(
      List<CoverageHole> base, List<CoverageHole> proposed, Set<String> changed) {
    Function<CoverageHole, String> key =
        h -> h.node() + "|" + h.family() + "|" + h.kind() + "|" + h.text();
    Set<String> before = new HashSet<>();
    base.forEach(h -> before.add(key.apply(h)));
    return proposed.stream()
        .filter(h -> changed.contains(h.node()))
        .filter(h -> !before.contains(key.apply(h)))
        .collect(ImmutableList.toImmutableList());
  }
}
