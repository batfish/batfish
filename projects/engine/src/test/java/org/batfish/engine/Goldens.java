package org.batfish.engine;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeSet;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.DataPlane;
import org.batfish.main.Batfish;

/**
 * Golden files for each fixture: the lifted facts, the coverage holes, and the computed RIB.
 *
 * <p>Regenerate after an intended change with {@code bazel run //projects/engine:update_goldens},
 * then review the diff like any other code change.
 */
final class Goldens {

  static final String ROOT = "projects/engine/src/test/resources/org/batfish/engine/golden";

  private Goldens() {}

  /** What a fixture produces, rendered deterministically. */
  record Rendered(String facts, String holes, String rib) {}

  static Rendered render(EngineBatfish engine, SnapshotRequest request) {
    SnapshotBuilder.Built built = new SnapshotBuilder(engine).build(request);
    Batfish bf = engine.batfish(built.id(), null);
    bf.computeDataPlane(EngineBatfish.snapshot(built.id()));
    return new Rendered(
        json(built.facts()),
        json(built.holes()),
        rib(bf.loadDataPlane(EngineBatfish.snapshot(built.id()))));
  }

  static String json(Object o) {
    try {
      return BatfishObjectMapper.writePrettyString(o) + "\n";
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException(e);
    }
  }

  /** One line per route: node, VRF, prefix, protocol, next hop, admin distance, metric. */
  static String rib(DataPlane dp) {
    TreeSet<String> lines = new TreeSet<>();
    dp.getRibs()
        .cellSet()
        .forEach(
            cell -> {
              for (AbstractRoute r : cell.getValue().getRoutes()) {
                lines.add(
                    String.join(
                        " ",
                        cell.getRowKey(),
                        cell.getColumnKey(),
                        r.getNetwork().toString(),
                        r.getProtocol().protocolName(),
                        r.getNextHop().toString(),
                        Long.toString(r.getAdministrativeCost()),
                        Long.toString(r.getMetric())));
              }
            });
    return String.join("\n", lines) + "\n";
  }

  static String read(String fixture, String file) {
    String path = "/org/batfish/engine/golden/" + fixture + "/" + file;
    try (InputStream in = Goldens.class.getResourceAsStream(path)) {
      if (in == null) {
        throw new IllegalStateException(
            "missing golden " + path + "; run bazel run //projects/engine:update_goldens");
      }
      return new String(in.readAllBytes(), UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /** Rewrites every golden in the source tree. Run through {@code bazel run}. */
  public static void main(String[] args) throws IOException {
    String workspace = System.getenv("BUILD_WORKSPACE_DIRECTORY");
    if (workspace == null) {
      throw new IllegalStateException("run with bazel run //projects/engine:update_goldens");
    }
    Path tmp = Files.createTempDirectory("engine-goldens");
    EngineBatfish engine = new EngineBatfish(tmp, 4);
    for (Map.Entry<String, SnapshotRequest> e : Fixtures.all().entrySet()) {
      Rendered r = render(engine, e.getValue());
      Path dir = Paths.get(workspace, ROOT, e.getKey());
      Files.createDirectories(dir);
      Files.writeString(dir.resolve("facts.json"), r.facts(), UTF_8);
      Files.writeString(dir.resolve("holes.json"), r.holes(), UTF_8);
      Files.writeString(dir.resolve("rib.txt"), r.rib(), UTF_8);
      System.out.println("wrote " + dir);
    }
  }
}
