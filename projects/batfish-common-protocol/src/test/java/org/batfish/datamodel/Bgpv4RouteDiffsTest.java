package org.batfish.datamodel;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests for {@link BgpRouteDiffs}. */
public class Bgpv4RouteDiffsTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new BgpRouteDiffs(ImmutableSet.of()), new BgpRouteDiffs(ImmutableSet.of()))
        .addEqualityGroup(
            new BgpRouteDiffs(ImmutableSet.of(new BgpRouteDiff(Bgpv4Route.PROP_AS_PATH, "B", "C"))))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() throws IOException {
    BgpRouteDiffs diffs =
        new BgpRouteDiffs(ImmutableSet.of(new BgpRouteDiff(Bgpv4Route.PROP_AS_PATH, "B", "C")));
    assertEquals(diffs, BatfishObjectMapper.clone(diffs, BgpRouteDiffs.class));

    diffs = new BgpRouteDiffs(ImmutableSet.of());
    assertEquals(diffs, BatfishObjectMapper.clone(diffs, BgpRouteDiffs.class));
  }
}
