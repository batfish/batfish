package org.batfish.datamodel.questions;

import static org.batfish.datamodel.BgpRoute.PROP_COMMUNITIES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.junit.Test;

/** Tests of {@link BgpRouteCommunityDiff}. */
public class BgpRouteCommunityDiffTest {

  /** Test that the added/removed sets are computed correctly. */
  @Test
  public void testSetsConstruction() {
    // [0:0, 1:1] -> [1:1, 2:2]
    SortedSet<Community> oldComms1 = new TreeSet<>();
    SortedSet<Community> newComms1 = new TreeSet<>();
    oldComms1.add(StandardCommunity.of(0, 0));
    oldComms1.add(StandardCommunity.of(1, 1));
    newComms1.add(StandardCommunity.of(1, 1));
    newComms1.add(StandardCommunity.of(2, 2));
    BgpRouteCommunityDiff comms1 = new BgpRouteCommunityDiff(oldComms1, newComms1);
    assertEquals(comms1.getAdded(), Set.of(StandardCommunity.of(2, 2)));
    assertEquals(comms1.getRemoved(), Set.of(StandardCommunity.of(0, 0)));
    assertEquals(comms1.getOldValue(), oldComms1);
    assertEquals(comms1.getNewValue(), newComms1);
  }

  @Test
  public void testProjection() {
    SortedSet<Community> oldComms = new TreeSet<>();
    SortedSet<Community> newComms = new TreeSet<>();
    oldComms.add(StandardCommunity.of(0, 0));
    newComms.add(StandardCommunity.of(1, 1));
    assertEquals(
        new BgpRouteDiff(PROP_COMMUNITIES, "[0:0]", "[1:1]"),
        new BgpRouteCommunityDiff(oldComms, newComms).toRouteDiff());
  }

  @Test
  public void testDeltaEquals() {
    // [0:0, 1:1] -> [1:1, 2:2]
    SortedSet<Community> oldComms1 = new TreeSet<>();
    SortedSet<Community> newComms1 = new TreeSet<>();
    oldComms1.add(StandardCommunity.of(0, 0));
    oldComms1.add(StandardCommunity.of(1, 1));
    newComms1.add(StandardCommunity.of(1, 1));
    newComms1.add(StandardCommunity.of(2, 2));
    BgpRouteCommunityDiff comms1 = new BgpRouteCommunityDiff(oldComms1, newComms1);

    // [0:0] -> [2:2]
    SortedSet<Community> oldComms2 = new TreeSet<>();
    SortedSet<Community> newComms2 = new TreeSet<>();
    oldComms2.add(StandardCommunity.of(0, 0));
    newComms2.add(StandardCommunity.of(2, 2));
    BgpRouteCommunityDiff comms2 = new BgpRouteCommunityDiff(oldComms2, newComms2);

    // These community diffs are delta equal
    assert (comms1.equals(comms2));
    // but not equal according to BgpRouteDiff
    assertNotEquals(comms1.toRouteDiff(), comms2.toRouteDiff());
  }
}
