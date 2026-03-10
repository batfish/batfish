package org.batfish.datamodel.questions;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.BgpRoute.PROP_COMMUNITIES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.google.common.collect.ImmutableSortedSet;
import java.util.Set;
import java.util.SortedSet;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.junit.Test;

/** Tests of {@link BgpRouteCommunityDiff}. */
public class BgpRouteCommunityDiffTest {

  private static SortedSet<Community> makeCommSet(int... comms) {
    checkArgument(comms.length % 2 == 0, "expecting even number of community parts");
    ImmutableSortedSet.Builder<Community> b = ImmutableSortedSet.naturalOrder();
    for (int i = 0; i < comms.length; i += 2) {
      b.add(StandardCommunity.of(comms[i], comms[i + 1]));
    }
    return b.build();
  }

  /** Test that the added/removed sets are computed correctly. */
  @Test
  public void testSetsConstruction() {
    // [0:0, 1:1] -> [1:1, 2:2]
    SortedSet<Community> oldComms1 = makeCommSet(0, 0, 1, 1);
    SortedSet<Community> newComms1 = makeCommSet(1, 1, 2, 2);
    BgpRouteCommunityDiff comms1 = new BgpRouteCommunityDiff(oldComms1, newComms1);
    assertEquals(comms1.getAdded(), Set.of(StandardCommunity.of(2, 2)));
    assertEquals(comms1.getRemoved(), Set.of(StandardCommunity.of(0, 0)));
    assertEquals(comms1.getOldValue(), oldComms1);
    assertEquals(comms1.getNewValue(), newComms1);
  }

  @Test
  public void testProjection() {
    SortedSet<Community> oldComms = makeCommSet(0, 0);
    SortedSet<Community> newComms = makeCommSet(1, 1);
    assertEquals(
        new BgpRouteDiff(PROP_COMMUNITIES, "[0:0]", "[1:1]"),
        new BgpRouteCommunityDiff(oldComms, newComms).toRouteDiff());
  }

  @Test
  public void testDeltaEquals() {
    // [0:0, 1:1] -> [1:1, 2:2]
    SortedSet<Community> oldComms1 = makeCommSet(0, 0, 1, 1);
    SortedSet<Community> newComms1 = makeCommSet(1, 1, 2, 2);
    BgpRouteCommunityDiff comms1 = new BgpRouteCommunityDiff(oldComms1, newComms1);

    // [0:0] -> [2:2]
    SortedSet<Community> oldComms2 = makeCommSet(0, 0);
    SortedSet<Community> newComms2 = makeCommSet(2, 2);
    BgpRouteCommunityDiff comms2 = new BgpRouteCommunityDiff(oldComms2, newComms2);

    // These community diffs are delta equal
    assert (comms1.equals(comms2));
    // but not equal according to BgpRouteDiff
    assertNotEquals(comms1.toRouteDiff(), comms2.toRouteDiff());
  }

  @Test
  public void testDeltaCompareTo() {
    // [0:0, 1:1] -> [1:1, 2:2]
    SortedSet<Community> oldComms1 = makeCommSet(0, 0, 1, 1);
    SortedSet<Community> newComms1 = makeCommSet(1, 1, 2, 2);
    BgpRouteCommunityDiff comms1 = new BgpRouteCommunityDiff(oldComms1, newComms1);

    // [0:0] -> [2:2]
    SortedSet<Community> oldComms2 = makeCommSet(0, 0);
    SortedSet<Community> newComms2 = makeCommSet(2, 2);
    BgpRouteCommunityDiff comms2 = new BgpRouteCommunityDiff(oldComms2, newComms2);

    // [0:0] -> [3:1]
    SortedSet<Community> oldComms3 = makeCommSet(0, 0);
    SortedSet<Community> newComms3 = makeCommSet(3, 1);
    BgpRouteCommunityDiff comms3 = new BgpRouteCommunityDiff(oldComms3, newComms3);

    // [0:0] -> []
    SortedSet<Community> oldComms4 = makeCommSet(0, 0);
    SortedSet<Community> newComms4 = makeCommSet();
    BgpRouteCommunityDiff comms4 = new BgpRouteCommunityDiff(oldComms4, newComms4);

    assert comms1.compareTo(comms2) == 0;
    assert comms1.compareTo(comms3) < 0;
    assert comms1.compareTo(comms4) > 0;
  }
}
