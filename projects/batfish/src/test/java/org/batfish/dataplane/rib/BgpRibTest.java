package org.batfish.dataplane.rib;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.OriginMechanism;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ReceivedFrom;
import org.batfish.datamodel.ReceivedFromInterface;
import org.batfish.datamodel.ReceivedFromIp;
import org.batfish.datamodel.ReceivedFromSelf;
import org.batfish.datamodel.ResolutionRestriction;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker;
import org.batfish.datamodel.bgp.NextHopIpTieBreaker;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;
import org.junit.Test;

/** Tests of {@link BgpRib} */
public class BgpRibTest {

  @Test
  public void testEvictSamePrefixReceivedFromPathId() {
    Bgpv4Rib bestPathRib =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ROUTER_ID,
            999,
            MultipathEquivalentAsPathMatchMode.PATH_LENGTH,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());
    Bgpv4Route.Builder rb =
        Bgpv4Route.builder()
            .setNetwork(Prefix.strict("10.0.0.1/32"))
            .setAdmin(200)
            .setClusterList(ImmutableSet.of(1L))
            .setLocalPreference(100L)
            .setMetric(0)
            .setNextHop(NextHopIp.of(Ip.parse("10.0.0.2")))
            .setOriginatorIp(Ip.parse("10.0.0.2"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("10.0.0.2")))
            .setReceivedFromRouteReflectorClient(true);
    Bgpv4Route rb1 =
        rb.setAsPath(AsPath.ofSingletonAsSets(1L, 21L, 3L))
            .setCommunities(CommunitySet.of(StandardCommunity.of(1, 1)))
            .build();
    Bgpv4Route rb2 =
        rb.setAsPath(AsPath.ofSingletonAsSets(1L, 22L, 3L))
            .setCommunities(CommunitySet.of(StandardCommunity.of(1, 2)))
            .build();
    bestPathRib.mergeRoute(rb1);
    RibDelta<Bgpv4Route> delta = bestPathRib.mergeRouteGetDelta(rb2);
    assertThat(delta, equalTo(RibDelta.builder().remove(rb1, Reason.WITHDRAW).add(rb2).build()));
  }

  @Test
  public void testMultipathMergeAndRemove_notMultipath() {
    Bgpv4Rib bgpRib =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ROUTER_ID,
            1,
            null,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());
    Bgpv4Route.Builder rb = Bgpv4Route.testBuilder().setNetwork(Prefix.ZERO);

    Bgpv4Route good =
        rb.setLocalPreference(20).setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.1"))).build();
    Bgpv4Route better =
        rb.setLocalPreference(30).setReceivedFrom(ReceivedFromIp.of(Ip.parse("2.2.2.2"))).build();
    Bgpv4Route best =
        rb.setLocalPreference(40).setReceivedFrom(ReceivedFromIp.of(Ip.parse("3.3.3.3"))).build();

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
  public void testMultipathMerge_order() {
    Bgpv4Route.Builder rb = Bgpv4Route.testBuilder().setNetwork(Prefix.ZERO);
    Supplier<Bgpv4Rib> makeRib =
        () ->
            new Bgpv4Rib(
                null,
                BgpTieBreaker.ROUTER_ID,
                null,
                MultipathEquivalentAsPathMatchMode.EXACT_PATH,
                false,
                LocalOriginationTypeTieBreaker.NO_PREFERENCE,
                NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
                NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
                ResolutionRestriction.alwaysTrue());

    Bgpv4Route nh1 =
        rb.setNextHop(NextHopIp.of(Ip.parse("1.1.1.1")))
            .setOriginatorIp(Ip.parse("1.0.0.1"))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.0.0.1")))
            .build();

    Bgpv4Route nh2better =
        rb.setNextHop(NextHopIp.of(Ip.parse("2.2.2.2")))
            .setOriginatorIp(Ip.parse("2.0.0.1"))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("2.0.0.1")))
            .build();

    Bgpv4Route nh2worse =
        rb.setNextHop(NextHopIp.of(Ip.parse("2.2.2.2")))
            .setOriginatorIp(Ip.parse("2.0.0.2"))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("2.0.0.2")))
            .build();

    Bgpv4Route nh3best =
        rb.setNextHop(NextHopIp.of(Ip.parse("3.3.3.3")))
            .setOriginatorIp(Ip.parse("3.0.0.1"))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("3.0.0.1")))
            .setLocalPreference(1000)
            .build();

    {
      Bgpv4Rib rib = makeRib.get();

      rib.mergeRoute(nh1);
      assertThat(rib.getRoutes(), contains(nh1));
      assertThat(rib.getRoutes(Prefix.ZERO), contains(nh1));

      rib.mergeRoute(nh2better);
      assertThat(rib.getRoutes(), containsInAnyOrder(nh1, nh2better));
      assertThat(rib.getRoutes(Prefix.ZERO), containsInAnyOrder(nh1, nh2better));

      assertThat(
          rib.multipathMergeRouteGetDelta(nh2worse).getMultipathDelta(), equalTo(RibDelta.empty()));
      assertThat(rib.getRoutes(), containsInAnyOrder(nh1, nh2better));
      assertThat(rib.getRoutes(Prefix.ZERO), containsInAnyOrder(nh1, nh2better));
    }
    {
      Bgpv4Rib rib = makeRib.get();

      rib.mergeRoute(nh1);
      assertThat(rib.getRoutes(), contains(nh1));
      assertThat(rib.getRoutes(Prefix.ZERO), contains(nh1));

      rib.mergeRoute(nh2worse);
      assertThat(rib.getRoutes(), containsInAnyOrder(nh1, nh2worse));
      assertThat(rib.getRoutes(Prefix.ZERO), containsInAnyOrder(nh1, nh2worse));

      assertThat(
          rib.multipathMergeRouteGetDelta(nh2better).getMultipathDelta(),
          equalTo(RibDelta.builder().remove(nh2worse, Reason.REPLACE).add(nh2better).build()));
      assertThat(rib.getRoutes(), containsInAnyOrder(nh1, nh2better));
      assertThat(rib.getRoutes(Prefix.ZERO), containsInAnyOrder(nh1, nh2better));

      assertThat(
          rib.multipathRemoveRouteGetDelta(nh2better).getMultipathDelta(),
          equalTo(RibDelta.builder().remove(nh2better, Reason.WITHDRAW).add(nh2worse).build()));
      assertThat(rib.getRoutes(), containsInAnyOrder(nh1, nh2worse));
      assertThat(rib.getRoutes(Prefix.ZERO), containsInAnyOrder(nh1, nh2worse));
    }
    {
      Bgpv4Rib rib = makeRib.get();

      rib.mergeRoute(nh2worse);
      assertThat(rib.getRoutes(), contains(nh2worse));
      assertThat(rib.getRoutes(Prefix.ZERO), contains(nh2worse));

      assertThat(
          rib.multipathMergeRouteGetDelta(nh2better).getMultipathDelta(),
          equalTo(RibDelta.builder().remove(nh2worse, Reason.REPLACE).add(nh2better).build()));
      assertThat(rib.getRoutes(), contains(nh2better));
      assertThat(rib.getRoutes(Prefix.ZERO), contains(nh2better));

      assertThat(
          rib.multipathMergeRouteGetDelta(nh3best).getMultipathDelta(),
          equalTo(RibDelta.builder().remove(nh2better, Reason.REPLACE).add(nh3best).build()));
      assertThat(rib.getRoutes(), contains(nh3best));
      assertThat(rib.getRoutes(Prefix.ZERO), contains(nh3best));

      assertThat(
          rib.multipathRemoveRouteGetDelta(nh3best).getMultipathDelta(),
          equalTo(RibDelta.builder().remove(nh3best, Reason.WITHDRAW).add(nh2better).build()));
      assertThat(rib.getRoutes(), contains(nh2better));
      assertThat(rib.getRoutes(Prefix.ZERO), contains(nh2better));
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
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());
    Bgpv4Route.Builder rb = Bgpv4Route.testBuilder().setNetwork(Prefix.ZERO);

    Bgpv4Route good =
        rb.setOriginatorIp(Ip.parse("1.1.1.40"))
            .setNextHop(NextHopIp.of(Ip.parse("1.1.1.40")))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.40")))
            .build();
    Bgpv4Route better =
        rb.setOriginatorIp(Ip.parse("1.1.1.30"))
            .setNextHop(NextHopIp.of(Ip.parse("1.1.1.30")))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.30")))
            .build();
    Bgpv4Route best =
        rb.setOriginatorIp(Ip.parse("1.1.1.20"))
            .setNextHop(NextHopIp.of(Ip.parse("1.1.1.20")))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.20")))
            .build();
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
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ROUTER_ID,
            1,
            null,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());
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
    3. Best ReceivedFrom
    4. Lowest path-id (null considered lowest)
    */
    List<Ip> decreasingIps = ImmutableList.of(Ip.parse("1.1.1.1"), Ip.parse("1.1.1.0"));
    List<Set<Long>> decreasingLengthClusterLists =
        ImmutableList.of(ImmutableSet.of(1L), ImmutableSet.of());
    List<Integer> decreasingPathIds = Arrays.asList(2, 1, null);
    List<Bgpv4Route> ordered = new ArrayList<>();
    for (Ip originatorIp : decreasingIps) {
      rb.setOriginatorIp(originatorIp);
      for (Set<Long> clusterList : decreasingLengthClusterLists) {
        rb.setClusterList(clusterList);
        for (Ip receivedFromIp : decreasingIps) {
          rb.setReceivedFrom(ReceivedFromIp.of(receivedFromIp));
          for (Integer pathId : decreasingPathIds) {
            ordered.add(rb.setPathId(pathId).build());
          }
        }
      }
    }

    for (int i = 0; i < ordered.size(); i++) {
      for (int j = 0; j < ordered.size(); j++) {
        assertThat(
            String.format("i=%d lhs=%s j=%d rhs=%s", i, ordered.get(i), j, ordered.get(j)),
            Integer.signum(rib.bestPathComparator(ordered.get(i), (ordered.get(j)))),
            equalTo(Integer.signum(i - j)));
      }
    }
  }

  @Test
  public void testCompareReceivedFrom() {
    // Preference order:
    //  1. Lowest receivedFromIp
    //  2. Best ReceivedFrom subtype (least -> most preferred: Interface, Ip, Self)
    //  3. String comparison on ReceivedFromInterface if applicable

    // r1 and r2 break tie on ReceivedFrom subtype
    ReceivedFrom r1 = ReceivedFromInterface.of("foo1", Ip.parse("169.254.0.2"));
    ReceivedFrom r2 = ReceivedFromIp.of(Ip.parse("169.254.0.2"));

    // r3 and r4 break tie on interface name
    ReceivedFrom r3 = ReceivedFromInterface.of("foo1", Ip.parse("169.254.0.1"));
    ReceivedFrom r4 = ReceivedFromInterface.of("foo2", Ip.parse("169.254.0.1"));

    // lowest IP so far
    ReceivedFrom r5 = ReceivedFromIp.of(Ip.parse("10.0.0.1"));
    // always lowest IP
    ReceivedFrom r6 = ReceivedFromSelf.instance();
    List<ReceivedFrom> ordered = ImmutableList.of(r1, r2, r3, r4, r5, r6);
    for (int i = 0; i < ordered.size(); i++) {
      for (int j = 0; j < ordered.size(); j++) {
        assertThat(
            String.format("i=%d lhs=%s j=%d rhs=%s", i, ordered.get(i), j, ordered.get(j)),
            Integer.signum(BgpRib.compareReceivedFrom(ordered.get(i), (ordered.get(j)))),
            equalTo(Integer.signum(i - j)));
      }
    }
  }

  @Test
  public void testBestPathComparator_ClusterListAsIgpCost() {
    BgpRib<Bgpv4Route> rib =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ROUTER_ID,
            1,
            null,
            true,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());
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
        3. Lowest receivedFromIp
    */
    List<Ip> decreasingIps = ImmutableList.of(Ip.parse("1.1.1.1"), Ip.parse("1.1.1.0"));
    List<Set<Long>> decreasingLengthClusterLists =
        ImmutableList.of(ImmutableSet.of(1L, 2L), ImmutableSet.of(1L), ImmutableSet.of());
    List<Bgpv4Route> ordered = new ArrayList<>();
    for (Set<Long> clusterList : decreasingLengthClusterLists) {
      rb.setClusterList(clusterList);
      for (Ip originatorIp : decreasingIps) {
        rb.setOriginatorIp(originatorIp);
        for (Ip receivedFromIp : decreasingIps) {
          ordered.add(rb.setReceivedFrom(ReceivedFromIp.of(receivedFromIp)).build());
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
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ROUTER_ID,
            1,
            null,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());
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
        3. Lowest receivedFromIp
    */
    List<Ip> decreasingIps = ImmutableList.of(Ip.parse("1.1.1.1"), Ip.parse("1.1.1.0"));
    List<Set<Long>> decreasingLengthClusterLists =
        ImmutableList.of(ImmutableSet.of(1L, 2L), ImmutableSet.of(1L), ImmutableSet.of());
    List<Bgpv4Route> ordered = new ArrayList<>();

    for (Ip originatorIp : decreasingIps) {
      rb.setOriginatorIp(originatorIp);
      for (Set<Long> clusterList : decreasingLengthClusterLists) {
        rb.setClusterList(clusterList);
        for (Ip receivedFromIp : decreasingIps) {
          ordered.add(rb.setReceivedFrom(ReceivedFromIp.of(receivedFromIp)).build());
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
  public void testArrivalOrderRemovesBackupRoutesToo() {
    Bgpv4Rib bestPathRib =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ARRIVAL_ORDER,
            999,
            MultipathEquivalentAsPathMatchMode.PATH_LENGTH,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());

    Bgpv4Route best =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.ZERO)
            .setNextHop(NextHopDiscard.instance())
            .setLocalPreference(1000L)
            .setOriginType(OriginType.IGP)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.1")))
            .setProtocol(RoutingProtocol.IBGP)
            .build();
    Bgpv4Route worse =
        best.toBuilder()
            .setLocalPreference(best.getLocalPreference() - 5)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.2")))
            .build();

    // SETUP, tested for setup correctly.
    // 1. Insert best and make sure it's best.
    bestPathRib.mergeRoute(best);
    assertThat(bestPathRib.getRoutes(), contains(best));
    assertThat(bestPathRib.getArrivalTimeForTesting(), aMapWithSize(1));
    // 2. Insert worse and make sure it's not best.
    bestPathRib.mergeRoute(worse);
    assertThat(bestPathRib.getRoutes(), contains(best));
    assertThat(bestPathRib.getArrivalTimeForTesting(), aMapWithSize(2));

    // TEST
    bestPathRib.removeRoute(worse);
    assertThat(bestPathRib.getRoutes(), contains(best));
    assertThat(bestPathRib.getArrivalTimeForTesting(), aMapWithSize(1));
    assertThat(bestPathRib.getArrivalTimeForTesting(), hasKey(best));
  }
}
