package org.batfish.engine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.batfish.datamodel.ExprAclLine;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Disk caches must not alias new snapshots after the engine JVM restarts. */
public final class EngineRestartTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void restartedEngineReparsesChangedAclInReusedStorage() throws Exception {
    Path storage = _folder.newFolder("storage").toPath();
    assertThat(buildInNewJvm(storage, "permit"), containsString("RULE:PERMIT"));
    assertThat(buildInNewJvm(storage, "deny"), containsString("RULE:DENY"));
  }

  private String buildInNewJvm(Path storage, String action) throws Exception {
    Path output = _folder.newFile().toPath();
    Process child =
        new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp",
                System.getProperty("java.class.path"),
                EngineRestartTest.class.getName(),
                storage.toString(),
                action)
            .redirectErrorStream(true)
            .redirectOutput(output.toFile())
            .start();
    try {
      assertThat("engine child timed out", child.waitFor(60, TimeUnit.SECONDS), equalTo(true));
      String text = Files.readString(output);
      assertThat(text, child.exitValue(), equalTo(0));
      return text;
    } finally {
      child.destroyForcibly();
    }
  }

  /** Separate process so all JVM-local snapshot counters and memory caches start empty. */
  public static void main(String[] args) {
    String config =
        "hostname edge\ninterface Ethernet1\n   no switchport\n"
            + "   ip address 192.0.2.1/24\n   ip access-group WEB in\n!\n"
            + "ip access-list WEB\n   10 "
            + args[1]
            + " tcp any any eq 443\n!\nend\n";
    SnapshotBuilder builder = new SnapshotBuilder(new EngineBatfish(Path.of(args[0]), 1));
    SnapshotBuilder.Built baseline =
        builder.build(
            new SnapshotRequest(
                List.of(
                    new SnapshotRequest.Device(
                        "edge", Dialect.ARISTA_EOS, config.replace("10 deny", "10 permit"))),
                null,
                null));
    SnapshotBuilder.Built built =
        new Deriver(builder)
            .derive(baseline, List.of(new Deriver.Change("edge", null, config)))
            .snapshot();
    ExprAclLine rule =
        (ExprAclLine)
            built.configurations().get("edge").getIpAccessLists().get("WEB").getLines().get(0);
    System.out.println("RULE:" + rule.getAction());
  }
}
