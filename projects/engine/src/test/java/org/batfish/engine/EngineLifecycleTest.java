package org.batfish.engine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThrows;

import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.batfish.identifiers.SnapshotId;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Failure paths must neither escape snapshot storage nor leave resources behind. */
public final class EngineLifecycleTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static SnapshotRequest.Device device(String name) {
    return new SnapshotRequest.Device(
        name,
        Dialect.ARISTA_EOS,
        "hostname " + name + "\ninterface Ethernet1\n no switchport\n ip address 192.0.2.1/24\n");
  }

  @Test
  public void unsafeNamesAreRejectedBeforeBuilding() {
    for (String name :
        List.of(
            "../escape",
            "/tmp/escape",
            "a/b",
            "a\\b",
            ".",
            "..",
            "",
            "r\nhostname other",
            "a".repeat(129))) {
      assertThrows(name, IllegalArgumentException.class, () -> device(name));
    }
    assertThrows(IllegalArgumentException.class, () -> device(null));
    assertThat(device("Site-1.Core_2").name(), equalTo("Site-1.Core_2"));
  }

  @Test
  public void httpRejectsUnsafeDeviceNameWithoutWritingInputs() throws Exception {
    Path storage = _folder.newFolder().toPath();
    try (EngineService service = new EngineService(new EngineBatfish(storage, 1));
        EngineServer server = new EngineServer(service, new InetSocketAddress("127.0.0.1", 0))) {
      server.start();
      HttpResponse<String> response =
          HttpClient.newHttpClient()
              .send(
                  HttpRequest.newBuilder(
                          URI.create("http://127.0.0.1:" + server.port() + "/snapshots/unsafe"))
                      .PUT(
                          HttpRequest.BodyPublishers.ofString(
                              "{\"devices\":[{\"name\":\"../escape\",\"dialect\":\"ARISTA_EOS\",\"configText\":\"hostname"
                                  + " r\\n"
                                  + "\"}]}"))
                      .header("Content-Type", "application/json")
                      .build(),
                  HttpResponse.BodyHandlers.ofString());
      assertThat(response.statusCode(), equalTo(400));
      try (var paths = Files.list(storage)) {
        assertThat(paths.count(), equalTo(0L));
      }
    }
  }

  @Test
  public void partialInputWriteFailureReclaimsTheSnapshot() throws Exception {
    EngineBatfish engine = new EngineBatfish(_folder.newFolder().toPath(), 1);
    SnapshotBuilder builder = new SnapshotBuilder(engine);
    SnapshotId id = EngineBatfish.freshSnapshotId("failed");
    Path dir = builder.snapshotDir(id);
    // First device writes successfully; the second write encounters a directory.
    Files.createDirectories(dir.resolve("input/configs/r2.cfg"));
    assertThrows(
        UncheckedIOException.class,
        () ->
            builder.build(
                id, new SnapshotRequest(List.of(device("r1"), device("r2")), null, null)));
    assertThat(Files.exists(dir), equalTo(false));
  }

  @Test
  public void dataPlaneFailureReclaimsDiskAndCachedConfigurations() throws Exception {
    EngineBatfish engine = new EngineBatfish(_folder.newFolder().toPath(), 1);
    AtomicReference<SnapshotBuilder.Built> attempted = new AtomicReference<>();
    try (EngineService service =
        new EngineService(
            engine,
            built -> {
              attempted.set(built);
              throw new IllegalStateException("deliberate dataplane failure");
            })) {
      String job =
          service.put("failed", new SnapshotRequest(List.of(device("r1")), null, null)).jobId();
      long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
      while (service.job(job).status() != EngineService.Status.failed
          && System.nanoTime() < deadline) {
        Thread.sleep(10);
      }
      assertThat(service.job(job).status(), equalTo(EngineService.Status.failed));
      SnapshotId id = attempted.get().id();
      assertThat(Files.exists(new SnapshotBuilder(engine).snapshotDir(id)), equalTo(false));
      assertThrows(
          RuntimeException.class,
          () -> engine.batfish(id, null).loadConfigurations(EngineBatfish.snapshot(id)));
      service.delete("failed");
      assertThrows(EngineService.EngineException.class, () -> service.snapshot("failed"));
    }
  }

  @Test
  public void closeTerminatesRequestWorkers() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(2);
    try (EngineService service =
            new EngineService(new EngineBatfish(_folder.newFolder().toPath(), 1));
        EngineServer server =
            new EngineServer(service, new InetSocketAddress("127.0.0.1", 0), executor)) {
      server.start();
      HttpResponse<String> response =
          HttpClient.newHttpClient()
              .send(
                  HttpRequest.newBuilder(
                          URI.create("http://127.0.0.1:" + server.port() + "/health"))
                      .GET()
                      .build(),
                  HttpResponse.BodyHandlers.ofString());
      assertThat(response.statusCode(), equalTo(200));
      server.close();
      assertThat(executor.awaitTermination(5, TimeUnit.SECONDS), equalTo(true));
    } finally {
      executor.shutdownNow();
    }
  }

  @Test
  public void failedBindClosesOwnedExecutor() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(1);
    try (EngineService service =
            new EngineService(new EngineBatfish(_folder.newFolder().toPath(), 1));
        EngineServer server = new EngineServer(service, new InetSocketAddress("127.0.0.1", 0))) {
      server.start();
      assertThrows(
          java.io.IOException.class,
          () ->
              new EngineServer(
                  service, new InetSocketAddress("127.0.0.1", server.port()), executor));
      assertThat(executor.isShutdown(), equalTo(true));
    } finally {
      executor.shutdownNow();
    }
  }
}
