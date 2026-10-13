package org.batfish.engine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Every fixture still lifts, reports holes, and computes routes exactly as reviewed. */
public final class GoldenTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void fixturesMatchGoldens() throws Exception {
    EngineBatfish engine = new EngineBatfish(_folder.newFolder().toPath(), 4);
    for (Map.Entry<String, SnapshotRequest> e : Fixtures.all().entrySet()) {
      Goldens.Rendered r = Goldens.render(engine, e.getValue());
      String fixture = e.getKey();
      assertThat(fixture + " facts", r.facts(), equalTo(Goldens.read(fixture, "facts.json")));
      assertThat(fixture + " holes", r.holes(), equalTo(Goldens.read(fixture, "holes.json")));
      assertThat(fixture + " rib", r.rib(), equalTo(Goldens.read(fixture, "rib.txt")));
    }
  }
}
