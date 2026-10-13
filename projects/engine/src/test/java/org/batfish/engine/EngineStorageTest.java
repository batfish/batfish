package org.batfish.engine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class EngineStorageTest {
  @Rule public TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void startupReclaimsOnlyThePreviousTenantCache() throws Exception {
    Path base = folder.newFolder().toPath();
    Path cache = base.resolve("networks/tenant/snapshots");
    try (EngineStorage first = EngineStorage.open(base)) {
      Files.createDirectories(cache.resolve("snap-old/input/configs"));
      Files.writeString(cache.resolve("snap-old/input/configs/edge.cfg"), "old config");
    }
    Path other = base.resolve("networks/other/keep");
    Files.createDirectories(other.getParent());
    Files.writeString(other, "other network");
    Files.writeString(base.resolve("keep"), "unrelated");
    try (EngineStorage second = EngineStorage.open(base)) {
      assertThat(Files.exists(cache), equalTo(false));
      assertThat(Files.readString(other), equalTo("other network"));
      assertThat(Files.readString(base.resolve("keep")), equalTo("unrelated"));
    }
  }

  @Test
  public void secondOwnerCannotRemoveAnActiveCache() throws Exception {
    Path base = folder.newFolder().toPath();
    Path config = base.resolve("networks/tenant/snapshots/snap-active/input/configs/edge.cfg");
    try (EngineStorage first = EngineStorage.open(base)) {
      Files.createDirectories(config.getParent());
      Files.writeString(config, "active");
      assertThrows(IOException.class, () -> EngineStorage.open(base));
      assertThat(Files.readString(config), equalTo("active"));
    }
    try (EngineStorage next = EngineStorage.open(base)) {
      assertThat(Files.exists(config), equalTo(false));
    }
  }

  @Test
  public void rejectedSameJvmOwnerDoesNotReleaseTheProcessLock() throws Exception {
    Path base = folder.newFolder().toPath();
    Path output = folder.newFile().toPath();
    try (EngineStorage owner = EngineStorage.open(base)) {
      assertThrows(IOException.class, () -> EngineStorage.open(base));
      Process child =
          new ProcessBuilder(
                  Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                  "-cp",
                  System.getProperty("java.class.path"),
                  EngineStorageTest.class.getName(),
                  base.toString())
              .redirectErrorStream(true)
              .redirectOutput(output.toFile())
              .start();
      try {
        assertThat(child.waitFor(10, TimeUnit.SECONDS), equalTo(true));
        assertThat(Files.readString(output), child.exitValue(), equalTo(0));
        assertThat(Files.readString(output).strip(), equalTo("locked"));
      } finally {
        child.destroyForcibly();
      }
    }
  }

  public static void main(String[] args) throws Exception {
    try (EngineStorage unexpected = EngineStorage.open(Path.of(args[0]))) {
      throw new AssertionError("another process acquired active storage");
    } catch (IOException expected) {
      if (!expected.getMessage().contains("already in use")) throw expected;
      System.out.println("locked");
    }
  }

  @Test
  public void startupRefusesSymlinkedCacheAndReleasesTheLock() throws Exception {
    Path base = folder.newFolder().toPath();
    Path external = folder.newFolder().toPath();
    Files.writeString(external.resolve("keep"), "preserve");
    Files.createSymbolicLink(base.resolve("networks"), external);
    assertThrows(IOException.class, () -> EngineStorage.open(base));
    assertThat(Files.readString(external.resolve("keep")), equalTo("preserve"));
    Files.delete(base.resolve("networks"));
    try (EngineStorage next = EngineStorage.open(base)) {
      assertThat(Files.readString(external.resolve("keep")), equalTo("preserve"));
    }
  }
}
