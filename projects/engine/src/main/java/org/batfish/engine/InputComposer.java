package org.batfish.engine;

import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.batfish.engine.SnapshotRequest.Device;
import org.batfish.engine.SnapshotRequest.LinuxInterface;

/** Turns a device's config text into the file Batfish parses, with its format pinned. */
public final class InputComposer {

  /**
   * The file Batfish parses, plus semantic gaps the dialect adapter knows about that Batfish's own
   * warnings would not reveal. Each gap is a {@code (family, text)} pair.
   */
  public record Composed(@Nonnull String text, @Nonnull List<Gap> gaps) {}

  /** A known difference between the device's behavior and what Batfish will model. */
  public record Gap(@Nonnull String family, @Nonnull String text) {}

  private static final Pattern FRR_VERSION = Pattern.compile("(?m)^frr version (\\d+)\\.(\\d+)");
  private static final Pattern ROUTER_BGP = Pattern.compile("(?m)^router bgp ");
  private static final Pattern NO_EBGP_REQUIRES_POLICY =
      Pattern.compile("^\\s*no bgp ebgp-requires-policy\\s*$");
  private static final Pattern BARE_EXIT = Pattern.compile("(?m)^[ \\t]*exit[ \\t]*\\n");
  private static final Pattern NO_EBGP_REQUIRES_POLICY_LINE =
      Pattern.compile("(?m)^[ \\t]*no bgp ebgp-requires-policy[ \\t]*\\n");

  private InputComposer() {}

  private static final Pattern HOSTNAME = Pattern.compile("(?m)^hostname\\s+\\S+[ \\t]*$");
  private static final Pattern JUNOS_HOST_NAME =
      Pattern.compile("(?m)^set system host-name\\s+\\S+[ \\t]*$");

  /** The snapshot input file for {@code device}. */
  public static @Nonnull Composed compose(@Nonnull Device device) {
    if (device.dialect() == Dialect.FRR_LINUX) {
      return composeFrrLinux(device);
    }
    return new Composed(
        "!RANCID-CONTENT-TYPE: "
            + device.dialect().rancidType()
            + "\n"
            + withHostname(device.configText(), device.dialect(), device.name()),
        ImmutableList.of());
  }

  /**
   * Batfish names a node after the hostname in its config. The engine's node is the tenant-unique
   * lowered name, which differs from the hostname when two sites reuse one, so the config's
   * hostname is set to it. A config without one gets one.
   */
  static @Nonnull String withHostname(
      @Nonnull String text, @Nonnull Dialect dialect, @Nonnull String name) {
    String lowered = name.toLowerCase(Locale.ROOT);
    if (dialect == Dialect.JUNOS) {
      Matcher m = JUNOS_HOST_NAME.matcher(text);
      return m.find()
          ? m.replaceFirst(Matcher.quoteReplacement("set system host-name " + lowered))
          : "set system host-name " + lowered + "\n" + text;
    }
    Matcher m = HOSTNAME.matcher(text);
    return m.find()
        ? m.replaceFirst(Matcher.quoteReplacement("hostname " + lowered))
        : "hostname " + lowered + "\n" + text;
  }

  /**
   * Batfish reads FRR only as a Cumulus-style concatenated file: hostname, then {@code
   * /etc/network/interfaces}, then {@code ports.conf}, then the FRR config. The interfaces section
   * is generated from the host's kernel interfaces, because that is where FRR hosts keep addresses.
   */
  private static Composed composeFrrLinux(Device device) {
    StringBuilder sb = new StringBuilder();
    sb.append(device.name().toLowerCase(Locale.ROOT)).append('\n');
    sb.append("# This file describes the network interfaces\n");
    List<LinuxInterface> interfaces = device.linuxInterfaces();
    if (interfaces != null) {
      for (LinuxInterface iface : interfaces) {
        sb.append("auto ").append(iface.name()).append('\n');
        sb.append("iface ").append(iface.name());
        if (iface.name().equals("lo")) {
          sb.append(" inet loopback");
        }
        sb.append('\n');
        for (String address : iface.addresses()) {
          sb.append("  address ").append(address).append('\n');
        }
      }
    }
    sb.append("# ports.conf --\n");
    String frr =
        withHostname(stripVtyshPreamble(device.configText()), Dialect.FRR_LINUX, device.name());
    ImmutableList.Builder<Gap> gaps = ImmutableList.builder();
    if (ebgpRequiresPolicy(frr)) {
      gaps.add(
          new Gap(
              CoverageHole.BGP,
              "FRR requires a policy on eBGP sessions by default (bgp ebgp-requires-policy);"
                  + " Batfish exchanges routes without one"));
    }
    // FRR 7.4+ prints a bare `exit` after each block, which Batfish's grammar predates; the block
    // structure does not depend on it. `no bgp ebgp-requires-policy` is what Batfish assumes
    // anyway.
    frr = BARE_EXIT.matcher(frr).replaceAll("");
    frr = NO_EBGP_REQUIRES_POLICY_LINE.matcher(frr).replaceAll("");
    sb.append(frr);
    if (!frr.endsWith("\n")) {
      sb.append('\n');
    }
    return new Composed(sb.toString(), gaps.build());
  }

  /**
   * Whether some BGP instance runs with FRR's default {@code bgp ebgp-requires-policy} (FRR 7.4 and
   * later), which Batfish does not model. Every distinct {@code router bgp} block needs its own
   * {@code no bgp ebgp-requires-policy}; a block may be re-entered later in the text, as proposed
   * changes do.
   */
  static boolean ebgpRequiresPolicy(String frr) {
    Matcher version = FRR_VERSION.matcher(frr);
    if (version.find()) {
      int major = Integer.parseInt(version.group(1));
      int minor = Integer.parseInt(version.group(2));
      if (major < 7 || (major == 7 && minor < 4)) {
        return false;
      }
    }
    Set<String> routers = new HashSet<>();
    Set<String> disabled = new HashSet<>();
    String current = null;
    for (String line : frr.split("\n", -1)) {
      if (line.startsWith("router bgp ")) {
        current = line.strip();
        routers.add(current);
      } else if (!line.isEmpty() && !Character.isWhitespace(line.charAt(0))) {
        current = null;
      } else if (current != null && NO_EBGP_REQUIRES_POLICY.matcher(line).matches()) {
        disabled.add(current);
      }
    }
    return !disabled.containsAll(routers);
  }

  /**
   * {@code vtysh -c "show running-config"} prints "Building configuration..." and "Current
   * configuration:" before the config itself, which starts at the {@code frr version} line.
   */
  static String stripVtyshPreamble(String text) {
    int start = text.indexOf("frr version");
    if (start <= 0) {
      return text;
    }
    String before = text.substring(0, start);
    for (String line : before.split("\n", -1)) {
      String t = line.strip();
      if (!t.isEmpty()
          && !t.equals("!")
          && !t.startsWith("Building configuration")
          && !t.startsWith("Current configuration")) {
        return text;
      }
    }
    return text.substring(start);
  }
}
