package org.batfish.engine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThrows;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.engine.Deriver.Change;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Portable snapshots and candidate semantics, exercising the real engine without HTTP. */
public final class SnapshotExportTest {
  @Rule public TemporaryFolder folder = new TemporaryFolder();

  private static void await(EngineService service, EngineService.JobRef ref) throws Exception {
    for (int i = 0; i < 600; i++) {
      EngineService.Job job = service.job(ref.jobId());
      if (job.status() == EngineService.Status.done) return;
      assertThat(job.error(), job.status(), not(equalTo(EngineService.Status.failed)));
      Thread.sleep(50);
    }
    throw new AssertionError("engine job timed out");
  }

  private static Map<String, String> files(byte[] bytes) throws Exception {
    Map<String, String> files = new TreeMap<>();
    try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
      ZipEntry entry;
      while ((entry = zip.getNextEntry()) != null) {
        files.put(entry.getName(), new String(zip.readAllBytes(), StandardCharsets.UTF_8));
      }
    }
    return files;
  }

  @Test
  public void exportedBaselineAndCandidateReproduceTheWithdrawal() throws Exception {
    EngineBatfish engine = new EngineBatfish(folder.newFolder().toPath(), 4);
    try (EngineService service = new EngineService(engine)) {
      SnapshotRequest request = Fixtures.frrOspfBgp();
      await(service, service.put("baseline", request));
      String candidate =
          request
              .devices()
              .get(0)
              .configText()
              .replaceAll("(?m)^[ \\t]*network 10\\.1\\.0\\.0/24[ \\t]*\\n", "");
      await(
          service,
          service.derive("baseline", "candidate", List.of(new Change("r1", null, candidate))));
      Map<String, String> before = files(service.exportSnapshot("baseline"));
      byte[] bytes = service.exportSnapshot("candidate");
      Map<String, String> after = files(bytes);
      assertThat(before.get("snapshot/configs/r1.cfg"), containsString("network 10.1.0.0/24"));
      assertThat(after.get("snapshot/configs/r1.cfg"), not(containsString("network 10.1.0.0/24")));
      assertThat(after.get("snapshot/configs/r1.cfg"), not(containsString("no network")));
      assertThat(
          after.get("snapshot/configs/r2.cfg"), equalTo(before.get("snapshot/configs/r2.cfg")));
      assertThat(after.keySet(), hasItem("snapshot/batfish/layer1_topology.json"));
      assertThat(service.exportSnapshot("candidate"), equalTo(bytes));
      var params =
          BatfishObjectMapper.mapper().valueToTree(Map.of("nodes", "r2", "network", "10.1.0.0/24"));
      var answer =
          BatfishObjectMapper.mapper().valueToTree(service.ask("candidate", "routes", params));
      assertThat(answer.path("answer").path("rows").size(), equalTo(0));
      var diff =
          BatfishObjectMapper.mapper()
              .valueToTree(service.diff("baseline", "candidate", "routes", params));
      assertThat(diff.toString(), containsString("Only in Reference"));

      // Reparse the exported files with Batfish's standard loader in fresh storage.
      EngineBatfish replay = new EngineBatfish(folder.newFolder().toPath(), 4);
      var id = EngineBatfish.freshSnapshotId("replay");
      var input = new SnapshotBuilder(replay).snapshotDir(id).resolve("input");
      for (var file : after.entrySet()) {
        var path = input.resolve(file.getKey().substring("snapshot/".length()));
        java.nio.file.Files.createDirectories(path.getParent());
        java.nio.file.Files.writeString(path, file.getValue());
      }
      var bf = replay.batfish(id, null);
      var snapshot = EngineBatfish.snapshot(id);
      bf.computeDataPlane(snapshot);
      assertThat(
          bf.loadDataPlane(snapshot)
              .getRibs()
              .get("r2", "default")
              .getRoutes(org.batfish.datamodel.Prefix.parse("10.1.0.0/24"))
              .size(),
          equalTo(0));
    }
  }

  @Test
  public void replacementRequiresOneUnambiguousInput() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> new Change("r1", null, null));
    assertThrows(
        IllegalArgumentException.class, () -> new Change("r1", List.of("end"), "hostname r1"));
    assertThrows(IllegalArgumentException.class, () -> new Change("r1", null, " "));
    Change parsed =
        BatfishObjectMapper.mapper()
            .readValue("{\"device\":\"r1\",\"configText\":\"hostname r1\\n\"}", Change.class);
    assertThat(parsed.configText(), equalTo("hostname r1\n"));
    assertThat(
        OpenApiContract.load()
            .checkRequest(
                "/snapshots/{id}/derive",
                "POST",
                BatfishObjectMapper.mapper()
                    .valueToTree(new EngineServer.DeriveRequest("candidate", List.of(parsed)))),
        equalTo(List.of()));
  }
}
