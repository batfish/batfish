package org.batfish.engine;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.common.runtime.SnapshotRuntimeData;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.engine.SnapshotRequest.Device;
import org.batfish.identifiers.SnapshotId;
import org.batfish.main.Batfish;

/**
 * Builds a snapshot from config text: writes the inputs, lets Batfish parse and convert them
 * through its normal path (finalization included), and reports what it could not model.
 *
 * <p>Loading the snapshot goes through Batfish's storage path, so post-processing (L1 topology,
 * interface dependencies, OSPF neighbors, the environment) runs as it would for any snapshot.
 */
public final class SnapshotBuilder {

  /**
   * A built snapshot: its id, the request it was built from, the models Batfish produced, each
   * device's neutral facts, and what Batfish could not model.
   */
  public record Built(
      @Nonnull SnapshotId id,
      @Nonnull SnapshotRequest request,
      @Nonnull SortedMap<String, Configuration> configurations,
      @Nonnull SortedMap<String, DeviceFacts> facts,
      @Nonnull List<CoverageHole> holes) {}

  /** The marker NOCFlow's relay redactor leaves in place of a dropped line. */
  static final String REDACTED_LINE = "NOCFLOW-REDACTED-LINE";

  private final EngineBatfish _engine;

  public SnapshotBuilder(@Nonnull EngineBatfish engine) {
    _engine = engine;
  }

  public @Nonnull Built build(@Nonnull SnapshotRequest request) {
    SnapshotId id = EngineBatfish.freshSnapshotId("snap");
    return build(id, request);
  }

  /** Builds under a caller-owned internal identity and reclaims partial work on failure. */
  Built build(SnapshotId id, SnapshotRequest request) {
    try {
      return buildInputs(id, request);
    } catch (RuntimeException | Error failure) {
      try {
        _engine.discard(id);
      } catch (RuntimeException cleanup) {
        failure.addSuppressed(cleanup);
      }
      throw failure;
    }
  }

  private Built buildInputs(SnapshotId id, SnapshotRequest request) {
    writeInputs(id, request);
    Batfish bf = _engine.batfish(id, null);
    NetworkSnapshot snapshot = EngineBatfish.snapshot(id);
    SortedMap<String, Configuration> configurations = bf.loadConfigurations(snapshot);
    ConvertConfigurationAnswerElement convert =
        bf.loadConvertConfigurationAnswerElementOrReparse(snapshot);
    List<CoverageHole> holes =
        new ArrayList<>(
            holes(
                request,
                configurations,
                bf.loadParseVendorConfigurationAnswerElement(snapshot),
                convert));
    ImmutableSortedMap.Builder<String, DeviceFacts> facts = ImmutableSortedMap.naturalOrder();
    for (Device device : request.devices()) {
      String node = device.name().toLowerCase(Locale.ROOT);
      Configuration c = configurations.get(node);
      if (c == null) {
        continue;
      }
      Lifter.Lifted lifted =
          Lifter.lift(
              node,
              c,
              InputComposer.compose(device).text(),
              "configs/" + fileName(device.name()),
              convert);
      facts.put(node, lifted.facts());
      holes.addAll(lifted.holes());
    }
    return new Built(id, request, configurations, facts.build(), ImmutableList.copyOf(holes));
  }

  /** The snapshot's directory on disk. */
  Path snapshotDir(SnapshotId id) {
    return _engine
        .storageBase()
        .resolve("networks")
        .resolve(EngineBatfish.NETWORK.getId())
        .resolve("snapshots")
        .resolve(id.getId());
  }

  private void writeInputs(SnapshotId id, SnapshotRequest request) {
    Path input = snapshotDir(id).resolve("input");
    Path configs = input.resolve("configs");
    Path batfishDir = input.resolve("batfish");
    try {
      Files.createDirectories(configs);
      Files.createDirectories(batfishDir);
      for (Device device : request.devices()) {
        Path target = configs.resolve(fileName(device.name())).normalize();
        if (!target.getParent().equals(configs.normalize())) {
          throw new IllegalArgumentException("device configuration must stay inside configs");
        }
        Files.writeString(target, InputComposer.compose(device).text(), UTF_8);
      }
      if (!request.layer1().isEmpty()) {
        Layer1Topology l1 =
            new Layer1Topology(
                request.layer1().stream()
                    .map(
                        l ->
                            new Layer1Edge(
                                l.node1().toLowerCase(Locale.ROOT),
                                l.interface1(),
                                l.node2().toLowerCase(Locale.ROOT),
                                l.interface2())));
        BatfishObjectMapper.mapper()
            .writeValue(batfishDir.resolve("layer1_topology.json").toFile(), l1);
      }
      Set<NodeInterfacePair> interfacesDown = new HashSet<>();
      request
          .environment()
          .interfacesDown()
          .forEach(
              i ->
                  interfacesDown.add(
                      NodeInterfacePair.of(i.node().toLowerCase(Locale.ROOT), i.iface())));
      for (Device device : request.devices()) {
        if (device.dialect() != Dialect.FRR_LINUX || device.linuxInterfaces() == null) continue;
        device.linuxInterfaces().stream()
            .filter(i -> !i.up())
            .forEach(
                i ->
                    interfacesDown.add(
                        NodeInterfacePair.of(device.name().toLowerCase(Locale.ROOT), i.name())));
      }
      if (!interfacesDown.isEmpty()) {
        SnapshotRuntimeData runtime =
            SnapshotRuntimeData.builder().setInterfacesLineDown(interfacesDown).build();
        BatfishObjectMapper.mapper()
            .writeValue(batfishDir.resolve("runtime_data.json").toFile(), runtime);
      }
      if (!request.environment().nodesDown().isEmpty()) {
        BatfishObjectMapper.mapper()
            .writeValue(
                batfishDir.resolve("node_blacklist").toFile(),
                request.environment().nodesDown().stream()
                    .map(n -> n.toLowerCase(Locale.ROOT))
                    .sorted()
                    .toList());
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  static String fileName(String node) {
    return node.toLowerCase(Locale.ROOT) + ".cfg";
  }

  private static List<CoverageHole> holes(
      SnapshotRequest request,
      Map<String, Configuration> configurations,
      ParseVendorConfigurationAnswerElement parse,
      ConvertConfigurationAnswerElement convert) {
    List<CoverageHole> holes = new ArrayList<>();
    for (Device device : request.devices()) {
      String node = device.name().toLowerCase(Locale.ROOT);
      String file = "configs/" + fileName(device.name());
      ParseStatus status = parse.getParseStatus().get(file);
      if (status != ParseStatus.PASSED && status != ParseStatus.PARTIALLY_UNRECOGNIZED) {
        holes.add(
            new CoverageHole(
                node, CoverageHole.UNCLASSIFIED, "unparsed", null, "parse status " + status));
        continue;
      }
      if (!configurations.containsKey(node)) {
        holes.add(
            new CoverageHole(
                node,
                CoverageHole.UNCLASSIFIED,
                "unparsed",
                null,
                "hostname in config does not match lowered name"));
      }
      // The relay's redactor drops a line it suspects of carrying an unknown secret form.
      // What the line configured is gone, so the device is not fully known.
      String[] lines = device.configText().split("\n", -1);
      for (int i = 0; i < lines.length; i++) {
        if (lines[i].contains(REDACTED_LINE)) {
          holes.add(
              new CoverageHole(
                  node,
                  CoverageHole.UNCLASSIFIED,
                  "redacted-line",
                  i + 1,
                  "a line was removed by redaction"));
        }
      }
      for (InputComposer.Gap gap : InputComposer.compose(device).gaps()) {
        holes.add(new CoverageHole(node, gap.family(), "dialect-gap", null, gap.text()));
      }
      Warnings pw = parse.getWarnings().get(file);
      if (pw != null) {
        for (ParseWarning w : pw.getParseWarnings()) {
          String kind =
              w.getComment().contains("not currently supported") ? "unimplemented" : "unrecognized";
          // An unrecognized line is classified by its own text: the parser context only says
          // which mode the parser was still in, which for an unknown top-level command is the
          // previous block's.
          String family =
              kind.equals("unrecognized")
                  ? CoverageHole.classify(w.getText())
                  : classify(w.getText(), w.getParserContext());
          holes.add(new CoverageHole(node, family, kind, w.getLine(), w.getText().strip()));
        }
        addWarnings(holes, node, "unimplemented", pw.getUnimplementedWarnings());
        addWarnings(holes, node, "redflag", pw.getRedFlagWarnings());
      }
      Warnings cw = convert.getWarnings().get(node);
      if (cw != null) {
        addWarnings(holes, node, "unimplemented", cw.getUnimplementedWarnings());
        addWarnings(holes, node, "redflag", cw.getRedFlagWarnings());
      }
      var undefined = convert.getUndefinedReferences().get(file);
      if (undefined != null) {
        undefined.forEach(
            (type, byName) ->
                byName
                    .keySet()
                    .forEach(
                        name ->
                            holes.add(
                                new CoverageHole(
                                    node,
                                    classify(type, ""),
                                    "undefined-reference",
                                    null,
                                    type + " " + name))));
      }
    }
    return ImmutableList.copyOf(holes);
  }

  private static void addWarnings(
      List<CoverageHole> holes, String node, String kind, Iterable<Warning> warnings) {
    for (Warning w : warnings) {
      holes.add(new CoverageHole(node, classify(w.getText(), ""), kind, null, w.getText()));
    }
  }

  /** Classify by the text first, then by the parser rule names, which name the mode. */
  static String classify(String text, String parserContext) {
    String family = CoverageHole.classify(text);
    if (!family.equals(CoverageHole.UNCLASSIFIED) || parserContext.isEmpty()) {
      return family;
    }
    String ctx = parserContext.toLowerCase(Locale.ROOT);
    if (ctx.contains("route_map") || ctx.contains("policy") || ctx.contains("prefix_list")) {
      return CoverageHole.POLICY;
    }
    if (ctx.contains("bgp")) {
      return CoverageHole.BGP;
    }
    if (ctx.contains("ospf")) {
      return CoverageHole.OSPF;
    }
    if (ctx.contains("acl") || ctx.contains("access_list")) {
      return CoverageHole.ACL;
    }
    if (ctx.contains("nat")) {
      return CoverageHole.NAT;
    }
    if (ctx.contains("static")) {
      return CoverageHole.STATIC;
    }
    if (ctx.contains("interface") || ctx.contains("if_")) {
      return CoverageHole.INTERFACES;
    }
    return CoverageHole.UNCLASSIFIED;
  }
}
