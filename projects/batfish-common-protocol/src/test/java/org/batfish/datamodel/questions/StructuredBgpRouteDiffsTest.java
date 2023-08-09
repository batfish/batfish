package org.batfish.datamodel.questions;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.testing.EqualsTester;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.junit.Test;

/** Tests for {@link StructuredBgpRouteDiffs} */
public class StructuredBgpRouteDiffsTest {
  @Test
  public void testEquals() {
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

    // comms1 and comms2 should be equal.
    new EqualsTester()
        .addEqualityGroup(new StructuredBgpRouteDiffs(), new StructuredBgpRouteDiffs())
        .addEqualityGroup(
            new StructuredBgpRouteDiffs(
                ImmutableSortedSet.of(new BgpRouteDiff(BgpRoute.PROP_AS_PATH, "B", "C")),
                Optional.of(comms1)),
            new StructuredBgpRouteDiffs(
                ImmutableSortedSet.of(new BgpRouteDiff(BgpRoute.PROP_AS_PATH, "B", "C")),
                Optional.of(comms2)))
        .addEqualityGroup(
            new StructuredBgpRouteDiffs(ImmutableSortedSet.of(), Optional.of(comms1)),
            new StructuredBgpRouteDiffs(ImmutableSortedSet.of(), Optional.of(comms2)))
        .testEquals();
  }

  @Test
  public void testHasDifferences() {

    StructuredBgpRouteDiffs d1 = new StructuredBgpRouteDiffs();
    assert (!d1.hasDifferences());

    SortedSet<Community> oldComms1 = new TreeSet<>();
    SortedSet<Community> newComms1 = new TreeSet<>();

    oldComms1.add(StandardCommunity.of(0, 0));
    oldComms1.add(StandardCommunity.of(1, 1));
    newComms1.add(StandardCommunity.of(1, 1));
    newComms1.add(StandardCommunity.of(2, 2));
    BgpRouteCommunityDiff comms1 = new BgpRouteCommunityDiff(oldComms1, newComms1);

    StructuredBgpRouteDiffs d2 =
        new StructuredBgpRouteDiffs(ImmutableSortedSet.of(), Optional.of(comms1));
    assert (d2.hasDifferences());

    StructuredBgpRouteDiffs d3 =
        new StructuredBgpRouteDiffs(
            ImmutableSortedSet.of(new BgpRouteDiff(BgpRoute.PROP_AS_PATH, "B", "C")),
            Optional.empty());

    assert (d3.hasDifferences());
  }
}
