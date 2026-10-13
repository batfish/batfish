package org.batfish.engine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import org.batfish.engine.SnapshotRequest.Device;
import org.batfish.engine.SnapshotRequest.Environment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Configs redacted by NOCFlow's relay compute exactly what the originals do. The {@code .redacted}
 * fixtures are the real redactor's output (packages/devices/interaction/src/redact-config.js).
 */
public final class RedactionTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private EngineBatfish _engine;

  @Before
  public void setUp() throws Exception {
    _engine = new EngineBatfish(_folder.newFolder().toPath(), 8);
  }

  @Test
  public void redactedFixturesComputeTheSameRoutes() {
    for (Map.Entry<String, SnapshotRequest> e : Fixtures.all().entrySet()) {
      SnapshotRequest redacted =
          Fixtures.withVariant(".redacted", () -> Fixtures.all().get(e.getKey()));
      assertThat(
          e.getKey(),
          Goldens.render(_engine, redacted).rib(),
          equalTo(Goldens.render(_engine, e.getValue()).rib()));
    }
  }

  /** Equal secrets on both ends get equal placeholders, so authenticated sessions still form. */
  @Test
  public void redactedAuthenticationKeysStillMatch() {
    String original = Goldens.render(_engine, Fixtures.frrOspfBgpAuth()).rib();
    String redacted =
        Goldens.render(_engine, Fixtures.withVariant(".redacted", Fixtures::frrOspfBgpAuth)).rib();

    assertThat(redacted, equalTo(original));
    assertThat(original, containsString("r1 default 2.2.2.2/32 ospf"));
    assertThat(original, containsString("r2 default 10.1.0.0/24 bgp"));
  }

  @Test
  public void aLineDroppedByRedactionIsAHole() {
    String text = Fixtures.text("dc1-eos/core1.cfg").replace("lldp run", "! NOCFLOW-REDACTED-LINE");
    SnapshotRequest request =
        new SnapshotRequest(
            ImmutableList.of(new Device("core1", Dialect.ARISTA_EOS, text)),
            ImmutableList.of(),
            Environment.NONE);

    var holes = new SnapshotBuilder(_engine).build(request).holes();

    assertThat(holes.stream().map(CoverageHole::kind).toList(), hasItem("redacted-line"));
  }
}
