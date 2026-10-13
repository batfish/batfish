package org.batfish.engine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.batfish.identifiers.SnapshotId;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class EngineRetentionTest {
  @Rule public TemporaryFolder folder = new TemporaryFolder();

  private static SnapshotRequest request() {
    return new SnapshotRequest(
        List.of(
            new SnapshotRequest.Device(
                "edge",
                Dialect.ARISTA_EOS,
                "hostname edge\ninterface Ethernet1\n no switchport\n ip address 192.0.2.1/24\n")),
        null,
        null);
  }

  private static void await(EngineService service, String job, EngineService.Status expected)
      throws Exception {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
    while (System.nanoTime() < deadline) {
      var status = service.job(job).status();
      if (status == expected) return;
      if (status == EngineService.Status.failed || status == EngineService.Status.done) {
        throw new AssertionError("unexpected terminal job: " + service.job(job));
      }
      Thread.sleep(10);
    }
    throw new AssertionError("job timed out");
  }

  @Test
  public void forwardingPoliciesNeverBecomeManagementGaps() {
    for (String command :
        List.of(
            "service-policy input DROP-WEB",
            "no service-policy type inspect INSPECT-WEB",
            "system mtu 9000",
            "system service-policy drop",
            "service unknown-forwarding-feature")) {
      assertThat(command, CoverageHole.classify(command), not(equalTo(CoverageHole.MANAGEMENT)));
      assertThat(
          command,
          SnapshotBuilder.classify(command, "service_policy"),
          not(equalTo(CoverageHole.MANAGEMENT)));
    }
    assertThat(
        CoverageHole.classify("service-policy input DROP-WEB"), equalTo(CoverageHole.POLICY));
    for (String command :
        List.of(
            "service password-encryption",
            "service timestamps log datetime",
            "no service sequence-numbers",
            "logging host 192.0.2.1",
            "hostname edge")) {
      assertThat(command, CoverageHole.classify(command), equalTo(CoverageHole.MANAGEMENT));
    }
  }

  @Test
  public void lruEvictionReclaimsModelsDiskAndJobsAndSupportsRebuild() throws Exception {
    EngineBatfish engine = new EngineBatfish(folder.newFolder().toPath(), 1);
    AtomicReference<SnapshotId> newest = new AtomicReference<>();
    try (EngineService service =
        new EngineService(engine, 2, b -> newest.set(b.id()), engine::discard)) {
      var a = service.put("a", request());
      await(service, a.jobId(), EngineService.Status.done);
      var b = service.put("b", request());
      await(service, b.jobId(), EngineService.Status.done);
      Path bDir = new SnapshotBuilder(engine).snapshotDir(newest.get());
      service.facts("a"); // a is most recently used.
      var c = service.put("c", request());
      await(service, c.jobId(), EngineService.Status.done);
      assertThat(service.snapshot("a").status(), equalTo(EngineService.Status.done));
      assertThat(
          assertThrows(EngineService.EngineException.class, () -> service.snapshot("b")).status(),
          equalTo(404));
      assertThat(
          assertThrows(EngineService.EngineException.class, () -> service.job(b.jobId())).status(),
          equalTo(404));
      assertThat(Files.exists(bDir), equalTo(false));
      var rebuilt = service.put("b", request());
      await(service, rebuilt.jobId(), EngineService.Status.done);
      assertThat(service.snapshot("b").status(), equalTo(EngineService.Status.done));
    }
  }

  @Test
  public void capacityDoesNotEvictRunningJobs() throws Exception {
    EngineBatfish engine = new EngineBatfish(folder.newFolder().toPath(), 1);
    CountDownLatch entered = new CountDownLatch(1);
    CountDownLatch release = new CountDownLatch(1);
    try (EngineService service =
        new EngineService(
            engine,
            1,
            b -> {
              entered.countDown();
              try {
                release.await();
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            },
            engine::discard)) {
      try {
        var job = service.put("running", request());
        assertThat(entered.await(10, TimeUnit.SECONDS), equalTo(true));
        assertThat(
            assertThrows(EngineService.EngineException.class, () -> service.put("other", request()))
                .status(),
            equalTo(429));
        release.countDown();
        await(service, job.jobId(), EngineService.Status.done);
      } finally {
        release.countDown();
      }
    }
  }

  @Test
  public void derivationKeepsItsBaseAndEvictsTheOtherSnapshot() throws Exception {
    EngineBatfish engine = new EngineBatfish(folder.newFolder().toPath(), 1);
    try (EngineService service = new EngineService(engine, 2, b -> {}, engine::discard)) {
      await(service, service.put("base", request()).jobId(), EngineService.Status.done);
      await(service, service.put("other", request()).jobId(), EngineService.Status.done);
      var change =
          service.derive(
              "base",
              "candidate",
              List.of(new Deriver.Change("edge", List.of("interface Ethernet1", " shutdown"))));
      await(service, change.jobId(), EngineService.Status.done);
      assertThat(
          service.facts("base").devices().get("edge").interfaces().get(0).adminUp(), equalTo(true));
      assertThat(
          service.facts("candidate").devices().get("edge").interfaces().get(0).adminUp(),
          equalTo(false));
      assertThrows(EngineService.EngineException.class, () -> service.snapshot("other"));
    }
  }

  @Test
  public void failedBuildCleanupCanBeRetriedThroughDelete() throws Exception {
    EngineBatfish engine = new EngineBatfish(folder.newFolder().toPath(), 1);
    AtomicBoolean failCleanup = new AtomicBoolean(true);
    AtomicReference<SnapshotId> built = new AtomicReference<>();
    try (EngineService service =
        new EngineService(
            engine,
            2,
            b -> {
              built.set(b.id());
              throw new IllegalStateException("compute failed");
            },
            id -> {
              if (failCleanup.get())
                throw new IllegalStateException("disk temporarily unavailable");
              engine.discard(id);
            })) {
      var job = service.put("failed", request());
      await(service, job.jobId(), EngineService.Status.failed);
      Path dir = new SnapshotBuilder(engine).snapshotDir(built.get());
      assertThat(Files.exists(dir), equalTo(true));
      assertThat(service.snapshot("failed").error(), containsString("snapshot cleanup failed"));
      assertThrows(IllegalStateException.class, () -> service.delete("failed"));
      assertThat(service.snapshot("failed").status(), equalTo(EngineService.Status.failed));
      failCleanup.set(false);
      service.delete("failed");
      assertThat(Files.exists(dir), equalTo(false));
      assertThrows(EngineService.EngineException.class, () -> service.snapshot("failed"));
      assertThrows(EngineService.EngineException.class, () -> service.job(job.jobId()));
    }
  }

  @Test
  public void failedDeleteRemainsRetryableAndCannotBeQueried() throws Exception {
    EngineBatfish engine = new EngineBatfish(folder.newFolder().toPath(), 1);
    AtomicBoolean failCleanup = new AtomicBoolean(false);
    try (EngineService service =
        new EngineService(
            engine,
            2,
            b -> {},
            id -> {
              if (failCleanup.get())
                throw new IllegalStateException("disk temporarily unavailable");
              engine.discard(id);
            })) {
      await(service, service.put("ready", request()).jobId(), EngineService.Status.done);
      failCleanup.set(true);
      assertThrows(IllegalStateException.class, () -> service.delete("ready"));
      assertThat(service.snapshot("ready").status(), equalTo(EngineService.Status.failed));
      assertThat(
          assertThrows(EngineService.EngineException.class, () -> service.facts("ready")).status(),
          equalTo(409));
      failCleanup.set(false);
      service.delete("ready");
      assertThrows(EngineService.EngineException.class, () -> service.snapshot("ready"));
    }
  }
}
