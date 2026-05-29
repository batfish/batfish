package org.batfish.datamodel.questions;

import static com.google.common.base.Preconditions.checkArgument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.testing.EqualsTester;
import java.util.Optional;
import java.util.SortedSet;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.junit.Test;

/** Tests for {@link StructuredBgpRouteDiffs} */
public class StructuredBgpRouteDiffsTest {
  private static SortedSet<Community> makeCommSet(int... comms) {
    checkArgument(comms.length % 2 == 0, "expecting even number of community parts");
    ImmutableSortedSet.Builder<Community> b = ImmutableSortedSet.naturalOrder();
    for (int i = 0; i < comms.length; i += 2) {
      b.add(StandardCommunity.of(comms[i], comms[i + 1]));
    }
    return b.build();
  }

  @Test
  public void testEquals() {
    // [0:0, 1:1] -> [1:1, 2:2]
    SortedSet<Community> oldComms1 = makeCommSet(0, 0, 1, 1);
    SortedSet<Community> newComms1 = makeCommSet(1, 1, 2, 2);
    BgpRouteCommunityDiff comms1 = new BgpRouteCommunityDiff(oldComms1, newComms1);

    // [0:0] -> [2:2]
    SortedSet<Community> oldComms2 = makeCommSet(0, 0);
    SortedSet<Community> newComms2 = makeCommSet(2, 2);
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
  public void testCompareTo() {
    // [0:0, 1:1] -> []
    SortedSet<Community> oldComms1 = makeCommSet(0, 0, 1, 1);
    SortedSet<Community> newComms1 = makeCommSet();
    BgpRouteCommunityDiff comms1 = new BgpRouteCommunityDiff(oldComms1, newComms1);

    StructuredBgpRouteDiffs d1 =
        new StructuredBgpRouteDiffs(
            ImmutableSortedSet.of(new BgpRouteDiff(BgpRoute.PROP_AS_PATH, "B", "C")),
            Optional.of(comms1));
    StructuredBgpRouteDiffs d2 =
        new StructuredBgpRouteDiffs(ImmutableSortedSet.of(), Optional.of(comms1));
    StructuredBgpRouteDiffs d3 =
        new StructuredBgpRouteDiffs(ImmutableSortedSet.of(), Optional.empty());
    StructuredBgpRouteDiffs d4 =
        new StructuredBgpRouteDiffs(
            ImmutableSortedSet.of(new BgpRouteDiff(BgpRoute.PROP_METRIC, "B", "C")),
            Optional.of(comms1));
    StructuredBgpRouteDiffs d5 =
        new StructuredBgpRouteDiffs(
            ImmutableSortedSet.of(new BgpRouteDiff(BgpRoute.PROP_AS_PATH, "B", "C")),
            Optional.of(comms1));

    assertThat(d1, greaterThan(d2));
    assertThat(d3, lessThan(d2));
    assertThat(d4, greaterThan(d2));
    assertThat(d1, lessThan(d4));
    assertThat(d1, comparesEqualTo(d5));
  }

  @Test
  public void testHasDifferences() {

    StructuredBgpRouteDiffs d1 = new StructuredBgpRouteDiffs();
    assertFalse(d1.hasDifferences());

    SortedSet<Community> oldComms1 = makeCommSet(0, 0, 1, 1);
    SortedSet<Community> newComms1 = makeCommSet(1, 1, 2, 2);
    BgpRouteCommunityDiff comms1 = new BgpRouteCommunityDiff(oldComms1, newComms1);

    StructuredBgpRouteDiffs d2 =
        new StructuredBgpRouteDiffs(ImmutableSortedSet.of(), Optional.of(comms1));
    assertTrue(d2.hasDifferences());

    StructuredBgpRouteDiffs d3 =
        new StructuredBgpRouteDiffs(
            ImmutableSortedSet.of(new BgpRouteDiff(BgpRoute.PROP_AS_PATH, "B", "C")),
            Optional.empty());

    assertTrue(d3.hasDifferences());
  }
}
