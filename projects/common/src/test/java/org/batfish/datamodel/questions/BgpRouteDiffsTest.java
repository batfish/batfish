package org.batfish.datamodel.questions;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests for {@link BgpRouteDiffs}. */
public class BgpRouteDiffsTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new BgpRouteDiffs(ImmutableSet.of()), new BgpRouteDiffs(ImmutableSet.of()))
        .addEqualityGroup(
            new BgpRouteDiffs(ImmutableSet.of(new BgpRouteDiff(BgpRoute.PROP_AS_PATH, "B", "C"))))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() {
    BgpRouteDiffs diffs =
        new BgpRouteDiffs(ImmutableSet.of(new BgpRouteDiff(BgpRoute.PROP_AS_PATH, "B", "C")));
    assertEquals(diffs, BatfishObjectMapper.clone(diffs, BgpRouteDiffs.class));

    diffs = new BgpRouteDiffs(ImmutableSet.of());
    assertEquals(diffs, BatfishObjectMapper.clone(diffs, BgpRouteDiffs.class));
  }
}
