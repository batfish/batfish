package org.batfish.dataplane.rib;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.junit.Test;

/** Tests of {@link BgpRib} */
public class BgpRibTest {

  @Test
  public void testMultipathMergeAndRemove_notMultipath() {
    Bgpv4Rib bgpRib = new Bgpv4Rib(null, BgpTieBreaker.ROUTER_ID, 1, null, false, null, null, null);
    Bgpv4Route.Builder rb = Bgpv4Route.testBuilder().setNetwork(Prefix.ZERO);

    Bgpv4Route good = rb.setLocalPreference(20).build();
    Bgpv4Route better = rb.setLocalPreference(30).build();
    Bgpv4Route best = rb.setLocalPreference(40).build();

    {
      // Merge into empty RIB
      BgpRib.MultipathRibDelta<Bgpv4Route> delta = bgpRib.multipathMergeRouteGetDelta(good);
      RibDelta<Bgpv4Route> expected = RibDelta.adding(good);
      assertThat(delta.getBestPathDelta(), equalTo(expected));
      assertThat(delta.getMultipathDelta(), equalTo(expected));
    }
    {
      // Merge a route that will beat the first route
      BgpRib.MultipathRibDelta<Bgpv4Route> delta = bgpRib.multipathMergeRouteGetDelta(best);
      RibDelta<Bgpv4Route> expected =
          RibDelta.<Bgpv4Route>builder()
              .remove(good, RouteAdvertisement.Reason.REPLACE)
              .add(best)
              .build();
      assertThat(delta.getBestPathDelta(), equalTo(expected));
      assertThat(delta.getMultipathDelta(), equalTo(expected));
    }
    {
      // Merge a route that is not as good
      BgpRib.MultipathRibDelta<Bgpv4Route> delta = bgpRib.multipathMergeRouteGetDelta(better);
      assertThat(delta.getBestPathDelta(), equalTo(RibDelta.empty()));
      assertThat(delta.getMultipathDelta(), equalTo(RibDelta.empty()));
    }
    {
      // Remove non-best route
      BgpRib.MultipathRibDelta<Bgpv4Route> delta = bgpRib.multipathRemoveRouteGetDelta(good);
      assertThat(delta.getBestPathDelta(), equalTo(RibDelta.empty()));
      assertThat(delta.getMultipathDelta(), equalTo(RibDelta.empty()));
    }
    {
      // Remove best route
      BgpRib.MultipathRibDelta<Bgpv4Route> delta = bgpRib.multipathRemoveRouteGetDelta(best);
      RibDelta<Bgpv4Route> expected =
          RibDelta.<Bgpv4Route>builder()
              .remove(best, RouteAdvertisement.Reason.WITHDRAW)
              .add(better)
              .build();
      assertThat(delta.getBestPathDelta(), equalTo(expected));
      assertThat(delta.getMultipathDelta(), equalTo(expected));
    }
    {
      // Remove last route
      BgpRib.MultipathRibDelta<Bgpv4Route> delta = bgpRib.multipathRemoveRouteGetDelta(better);
      RibDelta<Bgpv4Route> expected = RibDelta.of(RouteAdvertisement.withdrawing(better));
      assertThat(delta.getBestPathDelta(), equalTo(expected));
      assertThat(delta.getMultipathDelta(), equalTo(expected));
    }
  }

  @Test
  public void testMultipathMergeAndRemove_multipath() {
    Bgpv4Rib bgpRib =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ROUTER_ID,
            null,
            MultipathEquivalentAsPathMatchMode.EXACT_PATH,
            false,
            null,
            null,
            null);
    Bgpv4Route.Builder rb = Bgpv4Route.testBuilder().setNetwork(Prefix.ZERO);

    Bgpv4Route good = rb.setOriginatorIp(Ip.parse("1.1.1.40")).build();
    Bgpv4Route better = rb.setOriginatorIp(Ip.parse("1.1.1.30")).build();
    Bgpv4Route best = rb.setOriginatorIp(Ip.parse("1.1.1.20")).build();
    {
      // Merge into empty RIB
      BgpRib.MultipathRibDelta<Bgpv4Route> delta = bgpRib.multipathMergeRouteGetDelta(good);
      RibDelta<Bgpv4Route> expected = RibDelta.adding(good);
      assertThat(delta.getBestPathDelta(), equalTo(expected));
      assertThat(delta.getMultipathDelta(), equalTo(expected));
    }
    {
      // Merge an equally good route that replaces the best path
      BgpRib.MultipathRibDelta<Bgpv4Route> delta = bgpRib.multipathMergeRouteGetDelta(best);
      RibDelta<Bgpv4Route> expectedBestPath =
          RibDelta.<Bgpv4Route>builder()
              .remove(good, RouteAdvertisement.Reason.REPLACE)
              .add(best)
              .build();
      // Good route is still multipath-best, so should not be removed in multipath delta
      RibDelta<Bgpv4Route> expectedMultipath = RibDelta.adding(best);
      assertThat(delta.getBestPathDelta(), equalTo(expectedBestPath));
      assertThat(delta.getMultipathDelta(), equalTo(expectedMultipath));
    }
    {
      // Merge an equally good route that does not replace the best path
      BgpRib.MultipathRibDelta<Bgpv4Route> delta = bgpRib.multipathMergeRouteGetDelta(better);
      assertThat(delta.getBestPathDelta(), equalTo(RibDelta.empty()));
      assertThat(delta.getMultipathDelta(), equalTo(RibDelta.adding(better)));
    }
    {
      // Remove non-best route
      BgpRib.MultipathRibDelta<Bgpv4Route> delta = bgpRib.multipathRemoveRouteGetDelta(good);
      assertThat(delta.getBestPathDelta(), equalTo(RibDelta.empty()));
      assertThat(
          delta.getMultipathDelta(), equalTo(RibDelta.of(RouteAdvertisement.withdrawing(good))));
    }
    {
      // Remove best route
      BgpRib.MultipathRibDelta<Bgpv4Route> delta = bgpRib.multipathRemoveRouteGetDelta(best);
      RibDelta<Bgpv4Route> expectedBestPath =
          RibDelta.<Bgpv4Route>builder()
              .remove(best, RouteAdvertisement.Reason.WITHDRAW)
              .add(better)
              .build();
      RibDelta<Bgpv4Route> expectedMultipath = RibDelta.of(RouteAdvertisement.withdrawing(best));
      assertThat(delta.getBestPathDelta(), equalTo(expectedBestPath));
      assertThat(delta.getMultipathDelta(), equalTo(expectedMultipath));
    }
    {
      // Remove last route
      BgpRib.MultipathRibDelta<Bgpv4Route> delta = bgpRib.multipathRemoveRouteGetDelta(better);
      RibDelta<Bgpv4Route> expected = RibDelta.of(RouteAdvertisement.withdrawing(better));
      assertThat(delta.getBestPathDelta(), equalTo(expected));
      assertThat(delta.getMultipathDelta(), equalTo(expected));
    }
  }

  @Test
  public void testBestPathComparator() {
    BgpRib<Bgpv4Route> rib =
        new Bgpv4Rib(null, BgpTieBreaker.ROUTER_ID, 1, null, false, null, null, null);
    Bgpv4Route.Builder rb =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.ZERO)
            .setNextHop(NextHopDiscard.instance())
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.IBGP);
    /*
    Create routes to compare. With non-ARRIVAL_ORDER tiebreaker, preference should be:
    1. Lowest originator IP
    2. Shortest cluster list
    3. Lowest receivedFromIp, nulls first (first two properties are nonnull)
    */
    List<Ip> decreasingIps = ImmutableList.of(Ip.parse("1.1.1.1"), Ip.ZERO);
    List<Set<Long>> decreasingLengthClusterLists =
        ImmutableList.of(ImmutableSet.of(1L), ImmutableSet.of());
    List<Bgpv4Route> ordered = new ArrayList<>();
    for (Ip originatorIp : decreasingIps) {
      rb.setOriginatorIp(originatorIp);
      for (Set<Long> clusterList : decreasingLengthClusterLists) {
        rb.setClusterList(clusterList);
        ordered.add(rb.setReceivedFromIp(null).build());
        for (Ip receivedFromIp : decreasingIps) {
          ordered.add(rb.setReceivedFromIp(receivedFromIp).build());
        }
      }
    }

    for (int i = 0; i < ordered.size(); i++) {
      for (int j = 0; j < ordered.size(); j++) {
        assertThat(
            Integer.signum(rib.bestPathComparator(ordered.get(i), (ordered.get(j)))),
            equalTo(Integer.signum(i - j)));
      }
    }
  }

  @Test
  public void testBestPathComparator_ClusterListAsIgpCost() {
    BgpRib<Bgpv4Route> rib =
        new Bgpv4Rib(null, BgpTieBreaker.ROUTER_ID, 1, null, true, null, null, null);
    Bgpv4Route.Builder rb =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.ZERO)
            .setNextHop(NextHopDiscard.instance())
            .setOriginType(OriginType.IGP)
            .setOriginatorIp(Ip.ZERO)
            .setProtocol(RoutingProtocol.IBGP);
    /*
    Create routes to compare. With non-ARRIVAL_ORDER tiebreaker, preference should be:
        1. Shortest Cluster List
        2. Lowest originator IP
        3. Lowest receivedFromIp, nulls first (first two properties are nonnull)
    */
    List<Ip> decreasingIps = ImmutableList.of(Ip.parse("1.1.1.1"), Ip.ZERO);
    List<Set<Long>> decreasingLengthClusterLists =
        ImmutableList.of(ImmutableSet.of(1L, 2L), ImmutableSet.of(1L), ImmutableSet.of());
    List<Bgpv4Route> ordered = new ArrayList<>();
    for (Set<Long> clusterList : decreasingLengthClusterLists) {
      rb.setClusterList(clusterList);
      for (Ip originatorIp : decreasingIps) {
        rb.setOriginatorIp(originatorIp);
        ordered.add(rb.setReceivedFromIp(null).build());
        for (Ip receivedFromIp : decreasingIps) {
          ordered.add(rb.setReceivedFromIp(receivedFromIp).build());
        }
      }
    }

    for (int i = 0; i < ordered.size(); i++) {
      for (int j = 0; j < ordered.size(); j++) {
        assertThat(
            Integer.signum(rib.comparePreference(ordered.get(i), (ordered.get(j)))),
            equalTo(Integer.signum(i - j)));
      }
    }
  }

  @Test
  public void testBestPathComparator_Default() {
    BgpRib<Bgpv4Route> rib =
        new Bgpv4Rib(null, BgpTieBreaker.ROUTER_ID, 1, null, false, null, null, null);
    Bgpv4Route.Builder rb =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.ZERO)
            .setNextHop(NextHopDiscard.instance())
            .setOriginType(OriginType.IGP)
            .setOriginatorIp(Ip.ZERO)
            .setProtocol(RoutingProtocol.IBGP);
    /*
    Create routes to compare. With non-ARRIVAL_ORDER tiebreaker, preference should be:
        1. Lowest originator IP
        2. Shortest Cluster List
        3. Lowest receivedFromIp, nulls first (first two properties are nonnull)
    */
    List<Ip> decreasingIps = ImmutableList.of(Ip.parse("1.1.1.1"), Ip.ZERO);
    List<Set<Long>> decreasingLengthClusterLists =
        ImmutableList.of(ImmutableSet.of(1L, 2L), ImmutableSet.of(1L), ImmutableSet.of());
    List<Bgpv4Route> ordered = new ArrayList<>();

    for (Ip originatorIp : decreasingIps) {
      rb.setOriginatorIp(originatorIp);
      for (Set<Long> clusterList : decreasingLengthClusterLists) {
        rb.setClusterList(clusterList);
        ordered.add(rb.setReceivedFromIp(null).build());
        for (Ip receivedFromIp : decreasingIps) {
          ordered.add(rb.setReceivedFromIp(receivedFromIp).build());
        }
      }
    }

    for (int i = 0; i < ordered.size(); i++) {
      for (int j = 0; j < ordered.size(); j++) {
        assertThat(
            Integer.signum(rib.comparePreference(ordered.get(i), (ordered.get(j)))),
            equalTo(Integer.signum(i - j)));
      }
    }
  }
}
