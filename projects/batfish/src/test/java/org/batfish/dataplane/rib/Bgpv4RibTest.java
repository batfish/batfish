package org.batfish.dataplane.rib;

import static org.batfish.datamodel.MultipathEquivalentAsPathMatchMode.EXACT_PATH;
import static org.batfish.datamodel.OriginMechanism.NETWORK;
import static org.batfish.datamodel.OriginMechanism.REDISTRIBUTE;
import static org.batfish.datamodel.RoutingProtocol.CONNECTED;
import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.NO_PREFERENCE;
import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.PREFER_NETWORK;
import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.PREFER_REDISTRIBUTE;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.LOWEST_NEXT_HOP_IP;
import static org.batfish.dataplane.ibdp.TestUtils.annotateRoute;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.OriginMechanism;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ReceivedFromIp;
import org.batfish.datamodel.ReceivedFromSelf;
import org.batfish.datamodel.ResolutionRestriction;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker;
import org.batfish.datamodel.bgp.NextHopIpTieBreaker;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link Bgpv4Rib} */
@RunWith(JUnit4.class)
public class Bgpv4RibTest {

  private Bgpv4Route.Builder _rb;
  private Bgpv4Rib _multiPathRib;
  private Bgpv4Rib _bestPathRib;

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setup() {
    _rb = Bgpv4Route.testBuilder();
    _rb.setAsPath(AsPath.ofSingletonAsSets(1L, 2L))
        .setNetwork(Prefix.parse("10.0.0.0/8"))
        .setProtocol(RoutingProtocol.BGP)
        .setLocalPreference(100)
        .setClusterList(ImmutableSortedSet.of(3L, 4L))
        .setCommunities(ImmutableSortedSet.of(StandardCommunity.of(5L), StandardCommunity.of(6L)))
        .setOriginatorIp(Ip.parse("1.1.1.1"))
        .setOriginType(OriginType.IGP)
        .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.1")))
        .setSrcProtocol(CONNECTED)
        .setWeight(0);

    _multiPathRib =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ROUTER_ID,
            null,
            EXACT_PATH,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());
    _bestPathRib =
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
  }

  @Test
  public void testEvictSamePrefixReceivedFromPathId() {
    StaticRoute.Builder sb =
        StaticRoute.builder().setNextHop(NextHopDiscard.instance()).setAdministrativeCost(1);

    Rib mainRib = new Rib();

    Bgpv4Rib bestPathRib =
        new Bgpv4Rib(
            mainRib,
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
            .setOriginatorIp(Ip.parse("10.0.0.2"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("10.0.0.2")))
            .setReceivedFromRouteReflectorClient(true);
    Bgpv4Route rb1 =
        rb.setAsPath(AsPath.ofSingletonAsSets(1L, 21L, 3L))
            .setCommunities(CommunitySet.of(StandardCommunity.of(1, 1)))
            // unresolvable at first
            .setNextHop(NextHopIp.of(Ip.parse("10.0.0.2")))
            .build();
    Bgpv4Route rb2 =
        rb.setAsPath(AsPath.ofSingletonAsSets(1L, 22L, 3L))
            .setCommunities(CommunitySet.of(StandardCommunity.of(1, 2)))
            // resolvable
            .setNextHop(NextHopIp.of(Ip.parse("10.0.0.3")))
            .build();
    // Add rb1, which should not be active because its next hop is unresolvable.
    boolean rb1Active = bestPathRib.mergeRoute(rb1);
    assertFalse(rb1Active);

    // Add resolver for rb2
    mainRib.mergeRoute(annotateRoute(sb.setNetwork(Prefix.strict("10.0.0.3/32")).build()));
    // Add rb2, which should:
    // 1. Erase rb1 since it has same received-from, prefix, path-id
    // 2. Be active, since its next-hop is resolvable.
    RibDelta<Bgpv4Route> delta = bestPathRib.mergeRouteGetDelta(rb2);
    assertThat(delta, equalTo(RibDelta.adding(rb2)));

    RibDelta<Bgpv4Route> removalDelta = bestPathRib.removeRouteGetDelta(rb2);
    assertThat(removalDelta, equalTo(RibDelta.builder().remove(rb2, Reason.WITHDRAW).build()));

    // add resolver for rb1
    RibDelta<AnnotatedRoute<AbstractRoute>> rb1ResolverDelta =
        mainRib.mergeRouteGetDelta(
            annotateRoute(sb.setNetwork(Prefix.strict("10.0.0.2/32")).build()));
    // update resolvability in BGP RIB.
    bestPathRib.updateActiveRoutes(rb1ResolverDelta);
    // rb1 should not be activated now, since it should have been erased when rb2 was added.
    assertThat(bestPathRib.getUnannotatedRoutes(), not(hasItem(rb1)));
  }

  @Test
  public void testParameterValidationMaxPaths() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Invalid max-paths value");
    new Bgpv4Rib(
        null,
        BgpTieBreaker.ARRIVAL_ORDER,
        0,
        null,
        false,
        LocalOriginationTypeTieBreaker.NO_PREFERENCE,
        NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
        NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
        ResolutionRestriction.alwaysTrue());
  }

  @Test
  public void testParameterValidationMatchMode() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Multipath AS-Path-Match-mode must be specified");
    new Bgpv4Rib(
        null,
        BgpTieBreaker.ARRIVAL_ORDER,
        2,
        null,
        false,
        LocalOriginationTypeTieBreaker.NO_PREFERENCE,
        NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
        NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
        ResolutionRestriction.alwaysTrue());
  }

  @Test
  public void testParameterValidationMatchModeNullMaxPaths() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Multipath AS-Path-Match-mode must be specified");
    new Bgpv4Rib(
        null,
        BgpTieBreaker.ARRIVAL_ORDER,
        null,
        null,
        false,
        LocalOriginationTypeTieBreaker.NO_PREFERENCE,
        NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
        NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
        ResolutionRestriction.alwaysTrue());
  }

  @Test
  public void testIsMultipath() {
    Bgpv4Rib rib =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ARRIVAL_ORDER,
            1,
            null,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());
    assertFalse("MaxPaths=1, not multipath", rib.isMultipath());
    rib =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ARRIVAL_ORDER,
            2,
            EXACT_PATH,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());
    assertTrue("Maxpaths=2 -> multipath", rib.isMultipath());
    rib =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ARRIVAL_ORDER,
            null,
            EXACT_PATH,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());
    assertTrue("Maxpaths=null -> multipath", rib.isMultipath());
  }

  @Test
  public void testWeightPreference() {
    Bgpv4Route worse = _rb.setWeight(0).build();
    // Higher value is preferred
    Bgpv4Route best = _rb.setWeight(1).build();
    _multiPathRib.mergeRoute(worse);
    _multiPathRib.mergeRoute(best);

    assertThat(_multiPathRib.getUnannotatedRoutes(), contains(best));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(best));
  }

  @Test
  public void testLocalPreference() {
    Bgpv4Route worse = _rb.setLocalPreference(100).build();
    // Higher value is preferred
    Bgpv4Route best = _rb.setLocalPreference(200).build();
    _multiPathRib.mergeRoute(worse);
    _multiPathRib.mergeRoute(best);

    assertThat(_multiPathRib.getUnannotatedRoutes(), contains(best));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(best));
  }

  @Test
  public void testAggregatePreference() {
    Bgpv4Route worse = _rb.setProtocol(RoutingProtocol.BGP).build();
    Bgpv4Route best = _rb.setProtocol(RoutingProtocol.AGGREGATE).build();
    _multiPathRib.mergeRoute(worse);
    _multiPathRib.mergeRoute(best);

    assertThat(_multiPathRib.getUnannotatedRoutes(), contains(best));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(best));
  }

  @Test
  public void testAsPathLengthPreference() {
    Bgpv4Route worse = _rb.setAsPath(AsPath.ofSingletonAsSets(1L, 2L)).build();
    // shorter path is preferred
    Bgpv4Route best = _rb.setAsPath(AsPath.ofSingletonAsSets(3L)).build();
    _multiPathRib.mergeRoute(worse);
    _multiPathRib.mergeRoute(best);

    assertThat(_multiPathRib.getUnannotatedRoutes(), contains(best));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(best));
  }

  @Test
  public void testOriginTypePreference() {
    Bgpv4Route worst = _rb.setOriginType(OriginType.INCOMPLETE).build();
    Bgpv4Route medium = _rb.setOriginType(OriginType.EGP).build();
    Bgpv4Route best = _rb.setOriginType(OriginType.IGP).build();

    _multiPathRib.mergeRoute(worst);
    _multiPathRib.mergeRoute(medium);

    assertThat(_multiPathRib.getUnannotatedRoutes(), contains(medium));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(medium));

    _multiPathRib.mergeRoute(best);
    assertThat(_multiPathRib.getUnannotatedRoutes(), contains(best));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(best));
  }

  @Test
  public void testEIbgpProtocolPreference() {
    Bgpv4Route worse = _rb.setProtocol(RoutingProtocol.IBGP).build();
    Bgpv4Route best = _rb.setProtocol(RoutingProtocol.BGP).build();

    _multiPathRib.mergeRoute(worse);
    _multiPathRib.mergeRoute(best);

    assertThat(_multiPathRib.getUnannotatedRoutes(), contains(best));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(best));
  }

  @Test
  public void testMedPreference() {
    Bgpv4Route worse = _rb.setMetric(1).build();
    Bgpv4Route best = _rb.setMetric(0).build();

    _multiPathRib.mergeRoute(worse);
    _multiPathRib.mergeRoute(best);

    assertThat(_multiPathRib.getUnannotatedRoutes(), contains(best));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(best));
  }

  @Test
  public void testIgpCostPreferenceSingleResolutionStep() {
    Rib mainRib = new Rib();
    StaticRoute.Builder sb =
        StaticRoute.testBuilder().setAdministrativeCost(1).setNextHopInterface("eth0");
    mainRib.mergeRoute(
        annotateRoute(sb.setNetwork(Prefix.parse("5.5.5.5/32")).setMetric(1).build()));
    mainRib.mergeRoute(
        annotateRoute(sb.setNetwork(Prefix.parse("5.5.5.6/32")).setMetric(2).build()));

    Bgpv4Rib rib =
        new Bgpv4Rib(
            mainRib,
            BgpTieBreaker.ROUTER_ID,
            null,
            EXACT_PATH,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());

    Bgpv4Route worse = _rb.setNextHopIp(Ip.parse("5.5.5.6")).build();
    // Lower IGP cost to next hop is better
    Bgpv4Route best = _rb.setNextHopIp(Ip.parse("5.5.5.5")).build();

    rib.mergeRoute(worse);
    rib.mergeRoute(best);
    assertThat(rib.getUnannotatedRoutes(), contains(best));
    assertThat(rib.getBestPathRoutes(), contains(best));
  }

  @Test
  public void testIgpCostPreferenceDiscardWorst() {
    Rib mainRib = new Rib();
    StaticRoute.Builder sb = StaticRoute.testBuilder().setAdministrativeCost(1);
    mainRib.mergeRoute(
        annotateRoute(
            sb.setNetwork(Prefix.parse("5.5.5.5/32"))
                .setMetric(2)
                .setNextHopInterface("eth0")
                .build()));
    mainRib.mergeRoute(
        annotateRoute(
            sb.setNetwork(Prefix.parse("5.5.5.6/32"))
                .setMetric(1)
                .setNextHop(NextHopDiscard.instance())
                .build()));

    Bgpv4Rib rib =
        new Bgpv4Rib(
            mainRib,
            BgpTieBreaker.ROUTER_ID,
            null,
            EXACT_PATH,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());

    // Discard next hop is worse despite lower IGP cost
    Bgpv4Route worse = _rb.setNextHopIp(Ip.parse("5.5.5.6")).build();
    Bgpv4Route best = _rb.setNextHopIp(Ip.parse("5.5.5.5")).build();

    rib.mergeRoute(worse);
    rib.mergeRoute(best);
    assertThat(rib.getUnannotatedRoutes(), contains(best));
    assertThat(rib.getBestPathRoutes(), contains(best));
  }

  @Test
  public void testIgpCostPreferenceTwoResolutionSteps() {
    Rib mainRib = new Rib();
    StaticRoute.Builder sb =
        StaticRoute.testBuilder().setAdministrativeCost(1).setNextHopInterface("eth0");
    mainRib.mergeRoute(
        annotateRoute(sb.setNetwork(Prefix.strict("5.5.5.5/32")).setMetric(1).build()));
    mainRib.mergeRoute(
        annotateRoute(sb.setNetwork(Prefix.strict("5.5.5.6/32")).setMetric(2).build()));
    mainRib.mergeRoute(
        annotateRoute(
            _rb.setNetwork(Prefix.strict("4.4.4.6/32"))
                .setMetric(0L)
                .setNextHopIp(Ip.parse("5.5.5.6"))
                .build()));
    // Lower IGP cost to next hop is better
    mainRib.mergeRoute(
        annotateRoute(
            _rb.setNetwork(Prefix.strict("4.4.4.5/32"))
                .setMetric(0L)
                .setNextHopIp(Ip.parse("5.5.5.5"))
                .build()));

    Bgpv4Rib rib =
        new Bgpv4Rib(
            mainRib,
            BgpTieBreaker.ROUTER_ID,
            null,
            EXACT_PATH,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());

    Bgpv4Route worse =
        _rb.setNetwork(Prefix.strict("6.0.0.0/24")).setNextHopIp(Ip.parse("4.4.4.6")).build();
    // Lower IGP cost to next hop is better
    Bgpv4Route best =
        _rb.setNetwork(Prefix.strict("6.0.0.0/24")).setNextHopIp(Ip.parse("4.4.4.5")).build();

    rib.mergeRoute(worse);
    rib.mergeRoute(best);
    assertThat(rib.getUnannotatedRoutes(), contains(best));
    assertThat(rib.getBestPathRoutes(), contains(best));
  }

  @Test
  public void testMultipathAsPathModeExactPath() {
    Bgpv4Rib rib =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ROUTER_ID,
            null,
            EXACT_PATH,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());

    Bgpv4Route base = _rb.setAsPath(AsPath.ofSingletonAsSets(1L, 2L)).build();
    Bgpv4Route candidate1 =
        _rb.setAsPath(AsPath.ofSingletonAsSets(1L, 2L))
            .setNextHopIp(Ip.parse("5.5.5.5"))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("5.5.5.5")))
            .build();
    Bgpv4Route candidate2 =
        _rb.setAsPath(AsPath.ofSingletonAsSets(1L, 3L))
            .setNextHopIp(Ip.parse("5.5.5.6"))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("5.5.5.6")))
            .build();

    rib.mergeRoute(base);
    assertTrue("Exact AS path match, allow merge", rib.mergeRoute(candidate1));
    assertFalse("Not an exact AS path match, don't merge", rib.mergeRoute(candidate2));
    assertThat(rib.getUnannotatedRoutes(), hasSize(2));
    assertThat(rib.getBestPathRoutes(), hasSize(1));
  }

  @Test
  public void testMultipathAsPathModeFirstAs() {
    Bgpv4Rib rib =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ROUTER_ID,
            null,
            MultipathEquivalentAsPathMatchMode.FIRST_AS,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());

    Bgpv4Route base = _rb.setAsPath(AsPath.ofSingletonAsSets(1L, 2L)).build();
    Bgpv4Route candidate1 =
        _rb.setAsPath(AsPath.ofSingletonAsSets(1L, 3L))
            .setNextHopIp(Ip.parse("5.5.5.5"))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("5.5.5.5")))
            .build();
    Bgpv4Route candidate2 =
        _rb.setAsPath(AsPath.ofSingletonAsSets(2L, 3L))
            .setNextHopIp(Ip.parse("5.5.5.6"))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("5.5.5.6")))
            .build();

    rib.mergeRoute(base);
    assertTrue("Exact AS path match, allow merge", rib.mergeRoute(candidate1));
    assertFalse("Not an exact AS path match, don't merge", rib.mergeRoute(candidate2));
    assertThat(rib.getUnannotatedRoutes(), hasSize(2));
    assertThat(rib.getBestPathRoutes(), hasSize(1));
  }

  @Test
  public void testMultipathDiffOriginator() {
    Bgpv4Route bestPath = _rb.build();
    _multiPathRib.mergeRoute(
        _rb.setOriginatorIp(Ip.parse("2.2.2.2"))
            .setNextHop(NextHopIp.of(Ip.parse("2.2.2.2")))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("2.2.2.2")))
            .build());
    _multiPathRib.mergeRoute(
        _rb.setOriginatorIp(Ip.parse("2.2.2.3"))
            .setNextHop(NextHopIp.of(Ip.parse("2.2.2.3")))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("2.2.2.3")))
            .build());
    _multiPathRib.mergeRoute(bestPath);
    assertThat(_multiPathRib.getUnannotatedRoutes(), hasSize(3));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(bestPath));
  }

  @Test
  public void testMultipathArrivalOrder() {
    _multiPathRib =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ARRIVAL_ORDER,
            null,
            EXACT_PATH,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());
    Bgpv4Route best = _rb.setNextHop(NextHopIp.of(Ip.parse("2.2.2.1"))).build();
    Bgpv4Route earliest =
        _rb.setOriginatorIp(Ip.parse("2.2.2.2"))
            .setNextHop(NextHopIp.of(Ip.parse("2.2.2.2")))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("2.2.2.2")))
            .build();
    _multiPathRib.mergeRoute(earliest);
    _multiPathRib.mergeRoute(
        _rb.setOriginatorIp(Ip.parse("2.2.2.3"))
            .setNextHop(NextHopIp.of(Ip.parse("2.2.2.3")))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("2.2.2.3")))
            .build());
    _multiPathRib.mergeRoute(best);

    assertThat(_multiPathRib.getUnannotatedRoutes(), hasSize(3));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(earliest));
  }

  @Test
  public void testMultipathDiffClusterList() {
    _rb.setProtocol(RoutingProtocol.IBGP).setClusterList(ImmutableSortedSet.of());
    Bgpv4Route bestPath = _rb.setNextHop(NextHopIp.of(Ip.parse("2.2.2.2"))).build();
    _multiPathRib.mergeRoute(bestPath);
    _multiPathRib.mergeRoute(
        _rb.setClusterList(ImmutableSortedSet.of(11L))
            .setNextHop(NextHopIp.of(Ip.parse("2.2.2.3")))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("2.2.2.3")))
            .build());
    _multiPathRib.mergeRoute(
        _rb.setClusterList(ImmutableSortedSet.of(22L, 33L))
            .setNextHop(NextHopIp.of(Ip.parse("2.2.2.4")))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("2.2.2.4")))
            .build());

    assertThat(_multiPathRib.getUnannotatedRoutes(), hasSize(3));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(bestPath));
  }

  @Test
  public void testMultipathDiffNeighbor() {
    Bgpv4Route bestPath = _rb.build();
    _multiPathRib.mergeRoute(
        _rb.setReceivedFrom(ReceivedFromIp.of(Ip.parse("2.2.2.2")))
            .setNextHop(NextHopIp.of(Ip.parse("2.2.2.2")))
            .build());
    _multiPathRib.mergeRoute(
        _rb.setReceivedFrom(ReceivedFromIp.of(Ip.parse("2.2.2.3")))
            .setNextHop(NextHopIp.of(Ip.parse("2.2.2.3")))
            .build());
    _multiPathRib.mergeRoute(bestPath);

    assertThat(_multiPathRib.getUnannotatedRoutes(), hasSize(3));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(bestPath));
  }

  @Test
  public void testMultipathSamePath() {
    Bgpv4Route bestPath = _rb.build();
    _multiPathRib.mergeRoute(bestPath);
    _multiPathRib.mergeRoute(bestPath);
    _multiPathRib.mergeRoute(bestPath);

    assertThat(_multiPathRib.getUnannotatedRoutes(), hasSize(1));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(bestPath));
  }

  @Test
  public void testMultipathEviction() {
    _multiPathRib.mergeRoute(
        _rb.setOriginatorIp(Ip.parse("4.4.4.4"))
            .setNextHop(NextHopIp.of(Ip.parse("4.4.4.4")))
            .build());
    _multiPathRib.mergeRoute(
        _rb.setReceivedFrom(ReceivedFromIp.of(Ip.parse("2.2.2.2")))
            .setNextHop(NextHopIp.of(Ip.parse("2.2.2.2")))
            .build());
    _multiPathRib.mergeRoute(
        _rb.setReceivedFrom(ReceivedFromIp.of(Ip.parse("2.2.2.3")))
            .setNextHop(NextHopIp.of(Ip.parse("2.2.2.3")))
            .build());

    assertThat(_multiPathRib.getUnannotatedRoutes(), hasSize(3));
    assertThat(_multiPathRib.getBestPathRoutes(), hasSize(1));
    Bgpv4Route bestPath = _rb.setLocalPreference(1000).build();
    _multiPathRib.mergeRoute(bestPath);

    assertThat(_multiPathRib.getUnannotatedRoutes(), contains(bestPath));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(bestPath));
  }

  @Test
  public void testBestPathsEqualRoutesForBestPathRib() {
    Bgpv4Rib bestPathRib =
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
    Bgpv4Route bestPath = _rb.build();
    bestPathRib.mergeRoute(_rb.setReceivedFrom(ReceivedFromIp.of(Ip.parse("2.2.2.2"))).build());
    bestPathRib.mergeRoute(_rb.setReceivedFrom(ReceivedFromIp.of(Ip.parse("2.2.2.3"))).build());
    bestPathRib.mergeRoute(bestPath);

    assertThat(bestPathRib.getUnannotatedRoutes(), contains(bestPath));
    assertThat(bestPathRib.getBestPathRoutes(), contains(bestPath));
  }

  @Test
  public void testMergeMultiplePrefixes() {
    _multiPathRib.mergeRoute(_rb.build());
    _multiPathRib.mergeRoute(_rb.setNetwork(Prefix.parse("10.1.0.0/16")).build());
    _multiPathRib.mergeRoute(_rb.setNetwork(Prefix.parse("10.1.1.0/24")).build());

    assertThat(_multiPathRib.getUnannotatedRoutes(), hasSize(3));
    assertThat(_multiPathRib.getUnannotatedRoutes(), equalTo(_multiPathRib.getBestPathRoutes()));

    _multiPathRib.mergeRoute(
        _rb.setNetwork(Prefix.parse("10.1.1.0/24"))
            .setOriginatorIp(Ip.parse("22.22.22.22"))
            .setNextHop(NextHopIp.of(Ip.parse("22.22.22.22")))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("22.22.22.22")))
            .build());
    assertThat(_multiPathRib.getUnannotatedRoutes(), hasSize(4));
    assertThat(_multiPathRib.getBestPathRoutes(), hasSize(3));
  }

  @Test
  public void testBestPathSelectionWithoutTieBreaking() {
    _bestPathRib.mergeRoute(_rb.build());
    Bgpv4Route bestPath = _rb.setLocalPreference(200).build();
    _bestPathRib.mergeRoute(bestPath);

    assertThat(_bestPathRib.getUnannotatedRoutes(), contains(bestPath));
  }

  @Test
  public void testBestPathSelectionTieBreakRouterId() {
    _bestPathRib.mergeRoute(_rb.build());
    // Lower originator IP is better
    Bgpv4Route bestPath = _rb.setOriginatorIp(Ip.parse("1.1.0.1")).build();
    _bestPathRib.mergeRoute(bestPath);

    assertThat(_bestPathRib.getUnannotatedRoutes(), contains(bestPath));
  }

  @Test
  public void testBestPathSelectionTieBreakReceivedFrom() {
    _bestPathRib.mergeRoute(_rb.build());
    // Lower IP is better
    Bgpv4Route bestPath = _rb.setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.0.1"))).build();
    _bestPathRib.mergeRoute(bestPath);

    assertThat(_bestPathRib.getUnannotatedRoutes(), contains(bestPath));
  }

  @Test
  public void testBestPathSelectionTieBreakingArrivalOrder() {
    _bestPathRib =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ARRIVAL_ORDER,
            1,
            null,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());
    Bgpv4Route bestPath = _rb.build();
    _bestPathRib.mergeRoute(bestPath);
    // Oldest route should win despite newer having lower Originator IP
    _bestPathRib.mergeRoute(
        _rb.setOriginatorIp(Ip.parse("1.1.0.1"))
            .setNextHop(NextHopIp.of(Ip.parse("1.1.0.1")))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.0.1")))
            .build());

    assertThat(_bestPathRib.getUnannotatedRoutes(), contains(bestPath));
  }

  @Test
  public void testBestPathSelectionTieBreakingEbgpOnly() {
    _bestPathRib =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ARRIVAL_ORDER,
            1,
            null,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());
    Bgpv4Route bestPath =
        _rb.setProtocol(RoutingProtocol.IBGP).setClusterList(ImmutableSet.of()).build();
    Bgpv4Route earliestPath =
        _rb.setProtocol(RoutingProtocol.IBGP).setClusterList(ImmutableSet.of(3L)).build();
    // ibgp should not care about arrival order
    _bestPathRib.mergeRoute(earliestPath);
    _bestPathRib.mergeRoute(bestPath);

    assertThat(_bestPathRib.getUnannotatedRoutes(), contains(bestPath));
  }

  @Test
  public void testBestPathSelectionClusterListLengthTrue() {
    _bestPathRib =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ARRIVAL_ORDER,
            1,
            null,
            true,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());
    Bgpv4Route bestPath =
        _rb.setProtocol(RoutingProtocol.IBGP)
            .setClusterList(ImmutableSet.of())
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .build();
    Bgpv4Route earliestPath =
        _rb.setProtocol(RoutingProtocol.IBGP)
            .setClusterList(ImmutableSet.of(3L))
            .setOriginatorIp(Ip.ZERO)
            .build();
    _bestPathRib.mergeRoute(earliestPath);
    _bestPathRib.mergeRoute(bestPath);

    assertThat(_bestPathRib.getUnannotatedRoutes(), contains(bestPath));
  }

  @Test
  public void testBestPathSelectionClusterListLengthFalse() {
    _bestPathRib =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ARRIVAL_ORDER,
            1,
            null,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());
    Bgpv4Route bestPath =
        _rb.setProtocol(RoutingProtocol.IBGP)
            .setClusterList(ImmutableSet.of())
            .setOriginatorIp(Ip.ZERO)
            .build();
    Bgpv4Route earliestPath =
        _rb.setProtocol(RoutingProtocol.IBGP)
            .setClusterList(ImmutableSet.of(3L))
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .build();
    _bestPathRib.mergeRoute(earliestPath);
    _bestPathRib.mergeRoute(bestPath);

    assertThat(_bestPathRib.getUnannotatedRoutes(), contains(bestPath));
  }

  /** We should not merge routes for which next hop is unreachable */
  @Test
  public void testRejectNextHopUnreachable() {
    Rib mainRib = new Rib();
    Bgpv4Rib bgpRib =
        new Bgpv4Rib(
            mainRib,
            BgpTieBreaker.ARRIVAL_ORDER,
            1,
            null,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());
    Bgpv4Route route =
        _rb.setProtocol(RoutingProtocol.IBGP)
            .setClusterList(ImmutableSet.of())
            .setOriginatorIp(Ip.ZERO)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .build();
    assertTrue(bgpRib.mergeRouteGetDelta(route).isEmpty());

    // Now try with a route to NH
    mainRib.mergeRoute(
        new AnnotatedRoute<>(new ConnectedRoute(Prefix.parse("1.1.1.0/24"), "eth0"), "vrf"));
    assertThat(bgpRib.mergeRouteGetDelta(route).getRoutes(), contains(route));
  }

  /**
   * Accept routes with next link-local next hops, even when there is no route to it in the main
   * rib.
   */
  @Test
  public void testAcceptLinkLocalNextHopEvenWhenUnreachable() {
    Rib mainRib = new Rib();
    Bgpv4Rib bgpRib =
        new Bgpv4Rib(
            mainRib,
            BgpTieBreaker.ARRIVAL_ORDER,
            1,
            null,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());
    Bgpv4Route route =
        _rb.setProtocol(RoutingProtocol.IBGP)
            .setClusterList(ImmutableSet.of())
            .setOriginatorIp(Ip.ZERO)
            .setNextHopIp(Ip.parse("169.254.0.1"))
            .setNextHopInterface("eth0")
            .build();

    assertThat(bgpRib.mergeRouteGetDelta(route).getRoutes(), contains(route));
  }

  //////////////////////////////////////////////////////////////////////////////////

  // NOTE: Tests below are old and may be somewhat hard to comprehend.
  // For now, keeping them as regression tests.

  //////////////////////////////////////////////////////////////////////////////////

  private static void testBgpAsPathMultipathHelper(
      MultipathEquivalentAsPathMatchMode multipathEquivalentAsPathMatchMode,
      boolean expectRoute2,
      boolean expectRoute3a,
      boolean expectRoute3b,
      boolean expectRoute3c,
      boolean expectRoute3d) {
    /*
     * Properties of the routes
     */
    // Should appear only for path-length match
    AsPath asPath2 = AsPath.ofSingletonAsSets(2L, 4L, 6L);
    // Should appear only for first-as match and path-length match
    AsPath asPath3a = AsPath.ofSingletonAsSets(3L, 5L, 6L);
    // Should never appear
    AsPath asPath3b = AsPath.ofSingletonAsSets(3L, 4L, 4L, 6L);
    // Should always appear
    AsPath bestAsPath = AsPath.ofSingletonAsSets(3L, 4L, 6L);
    AsPath asPath3c = bestAsPath;
    AsPath asPath3d = bestAsPath;
    Ip nextHop2 = Ip.parse("2.0.0.0");
    Ip nextHop3a = Ip.parse("3.0.0.1");
    Ip nextHop3b = Ip.parse("3.0.0.2");
    Ip nextHop3c = Ip.parse("3.0.0.3");
    Ip nextHop3d = Ip.parse("3.0.0.4");

    /*
     * Common attributes for all routes
     */
    Prefix p = Prefix.ZERO;
    Bgpv4Route.Builder b =
        Bgpv4Route.testBuilder()
            .setNetwork(p)
            .setProtocol(RoutingProtocol.BGP)
            .setOriginType(OriginType.INCOMPLETE);

    Bgpv4Route bestRoute =
        b.setAsPath(bestAsPath)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.1.")))
            .build();

    /*
     * Instantiate routes
     */
    Bgpv4Route route2 =
        b.setAsPath(asPath2)
            .setNextHopIp(nextHop2)
            .setOriginatorIp(nextHop2)
            .setReceivedFrom(ReceivedFromIp.of(nextHop2))
            .build();
    Bgpv4Route route3a =
        b.setAsPath(asPath3a)
            .setNextHopIp(nextHop3a)
            .setOriginatorIp(nextHop3a)
            .setReceivedFrom(ReceivedFromIp.of(nextHop3a))
            .build();
    Bgpv4Route route3b =
        b.setAsPath(asPath3b)
            .setNextHopIp(nextHop3b)
            .setOriginatorIp(nextHop3b)
            .setReceivedFrom(ReceivedFromIp.of(nextHop3b))
            .build();
    Bgpv4Route route3c =
        b.setAsPath(asPath3c)
            .setNextHopIp(nextHop3c)
            .setOriginatorIp(nextHop3c)
            .setReceivedFrom(ReceivedFromIp.of(nextHop3c))
            .build();
    Bgpv4Route route3d =
        b.setAsPath(asPath3d)
            .setNextHopIp(nextHop3d)
            .setOriginatorIp(nextHop3d)
            .setReceivedFrom(ReceivedFromIp.of(nextHop3d))
            .build();

    Bgpv4Rib bmr =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ARRIVAL_ORDER,
            null,
            multipathEquivalentAsPathMatchMode,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());

    /*
     * Add routes to multipath RIB.
     */
    bmr.mergeRoute(bestRoute);
    bmr.mergeRoute(route2);
    bmr.mergeRoute(route3a);
    bmr.mergeRoute(route3b);
    bmr.mergeRoute(route3c);
    bmr.mergeRoute(route3d);

    /*
     * Initialize the matchers with respect to the output route set
     */
    Set<Bgpv4Route> postMergeRoutes = bmr.getRoutes();
    Matcher<Bgpv4Route> present = in(postMergeRoutes);
    Matcher<Bgpv4Route> absent = not(present);

    /*
     * ASSERTIONS:
     * Only the expected routes for the given match mode should be present at end
     */
    assertThat(route2, expectRoute2 ? present : absent);
    assertThat(route3a, expectRoute3a ? present : absent);
    assertThat(route3b, expectRoute3b ? present : absent);
    assertThat(route3c, expectRoute3c ? present : absent);
    assertThat(route3c, expectRoute3d ? present : absent);
  }

  @Test
  public void testBgpAsPathMultipathExactPath() {
    /*
     * Only routes with exact AS path match to that of best AS path should appear in RIB post-merge.
     */
    testBgpAsPathMultipathHelper(EXACT_PATH, false, false, false, true, true);
  }

  @Test
  public void testBgpAsPathMultipathFirstAs() {
    /*
     * Only routes with first-as matching that of best as path should appear in RIB post-merge.
     */
    testBgpAsPathMultipathHelper(
        MultipathEquivalentAsPathMatchMode.FIRST_AS, false, true, false, true, true);
  }

  @Test
  public void testBgpAsPathMultipathPathLength() {
    /*
     * All routes with same as-path-length as that of best as-path should appear in RIB post-merge.
     */
    testBgpAsPathMultipathHelper(
        MultipathEquivalentAsPathMatchMode.PATH_LENGTH, true, true, false, true, true);
  }

  @Test
  public void testBgpCompareOriginType() {
    Bgpv4Rib bbr =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ROUTER_ID,
            1,
            EXACT_PATH,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());
    Bgpv4Rib bmr =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ROUTER_ID,
            null,
            EXACT_PATH,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());

    Prefix p = Prefix.ZERO;
    Bgpv4Route.Builder b = Bgpv4Route.testBuilder().setNetwork(p).setProtocol(RoutingProtocol.IBGP);

    /*
     *  Initialize with different originator ips, which should not affect comparison of routes with
     *  different origin type.
     */
    Map<OriginType, List<Bgpv4Route>> routesByOriginType = new LinkedHashMap<>();
    for (OriginType originType : OriginType.values()) {
      List<Bgpv4Route> routes =
          routesByOriginType.computeIfAbsent(originType, o -> new ArrayList<>());
      routes.add(
          b.setOriginatorIp(Ip.ZERO)
              .setReceivedFrom(ReceivedFromIp.of(Ip.parse("192.0.2.1")))
              .setOriginType(originType)
              .build());
      routes.add(
          b.setOriginatorIp(Ip.MAX)
              .setReceivedFrom(ReceivedFromIp.of(Ip.parse("192.0.2.2")))
              .setOriginType(originType)
              .build());
    }

    /*
     * Whenever origin type is different, it should be overriding factor in preference.
     */
    for (OriginType o1 : OriginType.values()) {
      List<Bgpv4Route> lhsList = routesByOriginType.get(o1);
      for (OriginType o2 : OriginType.values()) {
        List<Bgpv4Route> rhsList = routesByOriginType.get(o2);
        for (Bgpv4Route lhs : lhsList) {
          for (Bgpv4Route rhs : rhsList) {
            if (o1.getPreference() > o2.getPreference()) {
              assertThat(bbr.comparePreference(lhs, rhs), greaterThan(0));
              assertThat(bmr.comparePreference(lhs, rhs), greaterThan(0));
            } else if (o1.getPreference() < o2.getPreference()) {
              assertThat(bbr.comparePreference(lhs, rhs), lessThan(0));
              assertThat(bmr.comparePreference(lhs, rhs), lessThan(0));
            }
          }
        }
      }
    }
  }

  @Test
  public void testContainsRoute() {

    Bgpv4Rib bbr =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ROUTER_ID,
            1,
            EXACT_PATH,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());
    Bgpv4Rib bmr =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ROUTER_ID,
            null,
            EXACT_PATH,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());
    Ip ip1 = Ip.parse("1.0.0.0");
    Ip ip2 = Ip.parse("2.2.0.0");
    Bgpv4Route.Builder b1 =
        Bgpv4Route.testBuilder()
            .setNextHop(NextHopIp.of(Ip.parse("3.3.3.3")))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("3.3.3.3")))
            .setOriginType(OriginType.INCOMPLETE)
            .setOriginatorIp(Ip.ZERO)
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("3.3.3.3")));
    Bgpv4Route.Builder b2 =
        Bgpv4Route.testBuilder()
            .setNextHop(NextHopIp.of(Ip.parse("3.3.3.4")))
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("3.3.3.4")))
            .setOriginType(OriginType.INCOMPLETE)
            .setOriginatorIp(Ip.MAX)
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFrom(ReceivedFromSelf.instance());

    /*
     * Toss a bunch of different routes in each RIB. In the best-path rib, only lower originatorIp
     * routes should remain. In the multipath RIB, all routes should remain.
     */
    for (int i = 8; i <= Prefix.MAX_PREFIX_LENGTH; i++) {
      Prefix p = Prefix.create(ip1, i);
      b1.setNetwork(p);
      b2.setNetwork(p);
      bbr.mergeRoute(b1.build());
      bbr.mergeRoute(b2.build());
      bmr.mergeRoute(b1.build());
      bmr.mergeRoute(b2.build());
    }
    for (int i = 16; i <= Prefix.MAX_PREFIX_LENGTH; i++) {
      Prefix p = Prefix.create(ip2, i);
      b1.setNetwork(p);
      b2.setNetwork(p);
      bbr.mergeRoute(b1.build());
      bbr.mergeRoute(b2.build());
      bmr.mergeRoute(b1.build());
      bmr.mergeRoute(b2.build());
    }
    for (int i = 8; i <= Prefix.MAX_PREFIX_LENGTH; i++) {
      Prefix p = Prefix.create(ip1, i);
      assertTrue(bbr.containsRoute(b1.setNetwork(p).build()));
      b1.setNetwork(p);
      b2.setNetwork(p);
      assertTrue(bbr.containsRoute(b1.build()));
      assertFalse(bbr.containsRoute(b2.build()));
      assertTrue(bmr.containsRoute(b1.build()));
      assertTrue(bmr.containsRoute(b2.build()));
    }
    for (int i = 16; i <= Prefix.MAX_PREFIX_LENGTH; i++) {
      Prefix p = Prefix.create(ip2, i);
      b1.setNetwork(p);
      b2.setNetwork(p);
      assertTrue(bbr.containsRoute(b1.build()));
      assertFalse(bbr.containsRoute(b2.build()));
      assertTrue(bmr.containsRoute(b1.build()));
      assertTrue(bmr.containsRoute(b2.build()));
    }
  }

  @Test
  public void testBgpTieBreaker() {
    // good for both ebgp and ibgp
    Bgpv4Rib bmr =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ROUTER_ID,
            null,
            EXACT_PATH,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());
    // ebgp
    Bgpv4Rib ebgpBpr =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ROUTER_ID,
            1,
            EXACT_PATH,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());
    Bgpv4Route.Builder ebgpBuilder =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.ZERO)
            .setOriginType(OriginType.INCOMPLETE)
            .setOriginatorIp(Ip.ZERO)
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFrom(ReceivedFromSelf.instance());
    Bgpv4Route ebgpOlderHigherOriginator =
        ebgpBuilder
            .setOriginatorIp(Ip.MAX)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.1")))
            .setNextHop(NextHopIp.of(Ip.parse("1.1.1.1")))
            .build();
    Bgpv4Route ebgpNewerHigherOriginator =
        ebgpBuilder
            .setOriginatorIp(Ip.MAX)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.2")))
            .setNextHop(NextHopIp.of(Ip.parse("1.1.1.2")))
            .build();
    Bgpv4Route ebgpLowerOriginator =
        ebgpBuilder.setOriginatorIp(Ip.ZERO).setNextHop(NextHopIp.of(Ip.parse("1.1.1.3"))).build();
    // ibgp
    Bgpv4Rib ibgpBpr =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ROUTER_ID,
            1,
            EXACT_PATH,
            false,
            LocalOriginationTypeTieBreaker.NO_PREFERENCE,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
            ResolutionRestriction.alwaysTrue());
    Bgpv4Route.Builder ibgpBuilder =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.ZERO)
            .setOriginType(OriginType.INCOMPLETE)
            .setOriginatorIp(Ip.ZERO)
            .setProtocol(RoutingProtocol.IBGP)
            .setReceivedFrom(ReceivedFromSelf.instance());
    Bgpv4Route ibgpOlderHigherOriginator =
        ibgpBuilder
            .setOriginatorIp(Ip.MAX)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.1")))
            .setNextHop(NextHopIp.of(Ip.parse("1.1.2.1")))
            .build();
    Bgpv4Route ibgpNewerHigherOriginator =
        ibgpBuilder
            .setOriginatorIp(Ip.MAX)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.2")))
            .setNextHop(NextHopIp.of(Ip.parse("1.1.2.2")))
            .build();
    Bgpv4Route ibgpLowerOriginator =
        ibgpBuilder.setOriginatorIp(Ip.ZERO).setNextHop(NextHopIp.of(Ip.parse("1.1.2.3"))).build();

    ebgpBpr.mergeRoute(ebgpOlderHigherOriginator);
    ibgpBpr.mergeRoute(ibgpOlderHigherOriginator);

    /*
     * Given default tie-breaking, and all more important attributes being equivalent:
     * - When comparing two eBGP adverts, best-path rib prefers older advert.
     * - If neither is older, or one is iBGP, best-path rib prefers advert with higher router-id.
     * - Multipath RIB ignores both age and router-id, seeing both adverts as equal.
     */

    // Test age comparisons first
    assertThat(
        ebgpBpr.comparePreference(ebgpNewerHigherOriginator, ebgpOlderHigherOriginator),
        lessThan(0));
    assertThat(bmr.comparePreference(ebgpNewerHigherOriginator, ebgpLowerOriginator), equalTo(0));
    assertThat(
        ibgpBpr.comparePreference(ibgpNewerHigherOriginator, ibgpLowerOriginator), lessThan(0));
    assertThat(bmr.comparePreference(ibgpNewerHigherOriginator, ibgpLowerOriginator), equalTo(0));
  }

  @Test
  public void testBgpRouteResolution() {
    Ip nhip = Ip.parse("1.1.1.1");
    Bgpv4Route dependentRoute =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.parse("5.0.0.0/8"))
            .setNextHop(NextHopIp.of(nhip))
            .build();
    AnnotatedRoute<AbstractRoute> resolvingRoute =
        new AnnotatedRoute<>(
            StaticRoute.testBuilder().setNetwork(nhip.toPrefix()).build(), "default");
    {
      // Main RIB does not initially contain resolving route
      Rib mainRib = new Rib();
      Bgpv4Rib bgpRib =
          new Bgpv4Rib(
              mainRib,
              BgpTieBreaker.ARRIVAL_ORDER,
              1,
              null,
              false,
              LocalOriginationTypeTieBreaker.NO_PREFERENCE,
              NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
              NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
              ResolutionRestriction.alwaysTrue());

      // Add dependent route. It should not be activated since it isn't resolvable in the main RIB
      assertThat(bgpRib.mergeRouteGetDelta(dependentRoute), equalTo(RibDelta.empty()));
      assertThat(bgpRib.getRoutes(), empty());

      // Add resolving route to main RIB and update BGP. Dependent route should be activated
      RibDelta<AnnotatedRoute<AbstractRoute>> mainRibDelta =
          mainRib.mergeRouteGetDelta(resolvingRoute);
      assertThat(
          bgpRib.updateActiveRoutes(mainRibDelta).getMultipathDelta(),
          equalTo(RibDelta.adding(dependentRoute)));
      assertThat(bgpRib.getRoutes(), contains(dependentRoute));
    }
    {
      // Main RIB initially does contain resolving route
      Rib mainRib = new Rib();
      mainRib.mergeRoute(resolvingRoute);
      Bgpv4Rib bgpRib =
          new Bgpv4Rib(
              mainRib,
              BgpTieBreaker.ARRIVAL_ORDER,
              1,
              null,
              false,
              LocalOriginationTypeTieBreaker.NO_PREFERENCE,
              NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
              NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
              ResolutionRestriction.alwaysTrue());

      // Add dependent route. It should be activated because it is resolvable in the main RIB
      assertThat(
          bgpRib.mergeRouteGetDelta(dependentRoute), equalTo(RibDelta.adding(dependentRoute)));
      assertThat(bgpRib.getRoutes(), contains(dependentRoute));

      // Remove resolving route from main RIB and update BGP. Dependent route should be deactivated
      RibDelta<AnnotatedRoute<AbstractRoute>> mainRibDelta =
          mainRib.removeRouteGetDelta(resolvingRoute);
      assertThat(
          bgpRib.updateActiveRoutes(mainRibDelta).getMultipathDelta(),
          equalTo(RibDelta.of(RouteAdvertisement.withdrawing(dependentRoute))));
      assertThat(bgpRib.getRoutes(), empty());

      // Re-add resolving route from main RIB and update BGP. Dependent route should be reactivated
      mainRibDelta = mainRib.mergeRouteGetDelta(resolvingRoute);
      assertThat(
          bgpRib.updateActiveRoutes(mainRibDelta).getMultipathDelta(),
          equalTo(RibDelta.of(RouteAdvertisement.adding(dependentRoute))));
      assertThat(bgpRib.getRoutes(), contains(dependentRoute));
    }
    {
      // Test of next hop IP LPM resolver restriction
      AnnotatedRoute<AbstractRoute> resolvingRoute1 =
          new AnnotatedRoute<>(new ConnectedRoute(nhip.toPrefix(), "foo"), "default");
      AnnotatedRoute<AbstractRoute> resolvingRoute2 =
          new AnnotatedRoute<>(new ConnectedRoute(nhip.toPrefix(), "bar"), "default");

      Rib mainRib = new Rib();
      Bgpv4Rib bgpRib =
          new Bgpv4Rib(
              mainRib,
              BgpTieBreaker.ARRIVAL_ORDER,
              1,
              null,
              false,
              LocalOriginationTypeTieBreaker.NO_PREFERENCE,
              NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
              NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP,
              r -> r.getAbstractRoute().getNextHop().equals(NextHopInterface.of("bar")));

      // Add dependent route. It should be inactive because there is no resolver in the main RIB
      assertThat(bgpRib.mergeRouteGetDelta(dependentRoute), equalTo(RibDelta.empty()));
      assertThat(bgpRib.getRoutes(), empty());

      // Add resolving route that DOES NOT pass restriction to main RIB and update BGP. No change
      // should occur to BGP RIB.
      RibDelta<AnnotatedRoute<AbstractRoute>> mainRibDelta =
          mainRib.mergeRouteGetDelta(resolvingRoute1);
      assertThat(
          bgpRib.updateActiveRoutes(mainRibDelta).getMultipathDelta(), equalTo(RibDelta.empty()));
      assertThat(bgpRib.getRoutes(), empty());

      // Add resolving route that DOES pass restriction to main RIB and update BGP. BGP RIB should
      // gain dependent route.
      mainRibDelta = mainRib.mergeRouteGetDelta(resolvingRoute2);
      assertThat(
          bgpRib.updateActiveRoutes(mainRibDelta).getMultipathDelta(),
          equalTo(RibDelta.of(RouteAdvertisement.adding(dependentRoute))));
      assertThat(bgpRib.getRoutes(), contains(dependentRoute));
    }
  }

  @Test
  public void testRedistributeRoutes() {
    Ip lowestNhip = Ip.parse("10.0.0.1");
    Ip highestNhip = Ip.parse("10.0.0.2");
    Prefix prefix = Prefix.strict("10.1.0.0/24");
    {
      // Test redistribute, preferring lowest NHIP
      Bgpv4Route lowestNhipRoute =
          Bgpv4Route.testBuilder()
              .setNetwork(prefix)
              .setNextHop(NextHopIp.of(lowestNhip))
              .setOriginMechanism(REDISTRIBUTE)
              .setSrcProtocol(CONNECTED)
              .build();
      Bgpv4Route highestNhipRoute =
          Bgpv4Route.testBuilder()
              .setNetwork(prefix)
              .setNextHop(NextHopIp.of(highestNhip))
              .setOriginMechanism(REDISTRIBUTE)
              .setSrcProtocol(CONNECTED)
              .build();
      Rib mainRib = new Rib();
      Bgpv4Rib bgpRib =
          new Bgpv4Rib(
              mainRib,
              BgpTieBreaker.ARRIVAL_ORDER,
              1,
              null,
              false,
              NO_PREFERENCE,
              HIGHEST_NEXT_HOP_IP,
              LOWEST_NEXT_HOP_IP,
              ResolutionRestriction.alwaysTrue());

      // Add less preferred NHIP route
      assertThat(
          bgpRib.mergeRouteGetDelta(highestNhipRoute), equalTo(RibDelta.adding(highestNhipRoute)));
      assertThat(bgpRib.getRoutes(), contains(highestNhipRoute));

      // Add more preferred NHIP route. Less preferred route should be removed, and should not
      // appear in backup.
      assertThat(
          bgpRib.mergeRouteGetDelta(lowestNhipRoute).stream()
              .collect(ImmutableList.toImmutableList()),
          containsInAnyOrder(
              RouteAdvertisement.adding(lowestNhipRoute),
              RouteAdvertisement.withdrawing(highestNhipRoute)));
      assertThat(bgpRib.getBackupRoutes(), not(hasItem(highestNhipRoute)));

      // Remove less preferred NHIP route. There should be no delta.
      assertThat(bgpRib.removeRouteGetDelta(highestNhipRoute), equalTo(RibDelta.empty()));

      // Re-add less preferred NHIP route. There should be no delta.
      assertThat(bgpRib.mergeRouteGetDelta(highestNhipRoute), equalTo(RibDelta.empty()));

      // Remove more preferred NHIP route. The less preferred one should now be added.
      assertThat(
          bgpRib.removeRouteGetDelta(lowestNhipRoute).stream()
              .collect(ImmutableList.toImmutableList()),
          containsInAnyOrder(
              RouteAdvertisement.adding(highestNhipRoute),
              RouteAdvertisement.withdrawing(lowestNhipRoute)));
    }
    {
      // Test redistribute, preferring highest NHIP
      Bgpv4Route lowestNhipRoute =
          Bgpv4Route.testBuilder()
              .setNetwork(prefix)
              .setNextHop(NextHopIp.of(lowestNhip))
              .setOriginMechanism(REDISTRIBUTE)
              .setSrcProtocol(CONNECTED)
              .build();
      Bgpv4Route highestNhipRoute =
          Bgpv4Route.testBuilder()
              .setNetwork(prefix)
              .setNextHop(NextHopIp.of(highestNhip))
              .setOriginMechanism(REDISTRIBUTE)
              .setSrcProtocol(CONNECTED)
              .build();
      Rib mainRib = new Rib();
      Bgpv4Rib bgpRib =
          new Bgpv4Rib(
              mainRib,
              BgpTieBreaker.ARRIVAL_ORDER,
              1,
              null,
              false,
              NO_PREFERENCE,
              LOWEST_NEXT_HOP_IP, // different on purpose
              HIGHEST_NEXT_HOP_IP,
              ResolutionRestriction.alwaysTrue());

      // Add less preferred NHIP route
      assertThat(
          bgpRib.mergeRouteGetDelta(lowestNhipRoute), equalTo(RibDelta.adding(lowestNhipRoute)));
      assertThat(bgpRib.getRoutes(), contains(lowestNhipRoute));

      // Add more preferred NHIP route. Less preferred route should be removed, and should not
      // appear in backup.
      assertThat(
          bgpRib.mergeRouteGetDelta(highestNhipRoute).stream()
              .collect(ImmutableList.toImmutableList()),
          containsInAnyOrder(
              RouteAdvertisement.adding(highestNhipRoute),
              RouteAdvertisement.withdrawing(lowestNhipRoute)));
      assertThat(bgpRib.getBackupRoutes(), not(hasItem(lowestNhipRoute)));

      // Remove less preferred NHIP route. There should be no delta.
      assertThat(bgpRib.removeRouteGetDelta(lowestNhipRoute), equalTo(RibDelta.empty()));

      // Re-add less preferred NHIP route. There should be no delta.
      assertThat(bgpRib.mergeRouteGetDelta(lowestNhipRoute), equalTo(RibDelta.empty()));

      // Remove more preferred NHIP route. The less preferred one should now be added.
      assertThat(
          bgpRib.removeRouteGetDelta(highestNhipRoute).stream()
              .collect(ImmutableList.toImmutableList()),
          containsInAnyOrder(
              RouteAdvertisement.adding(lowestNhipRoute),
              RouteAdvertisement.withdrawing(highestNhipRoute)));
    }
    {
      // Test network, preferring lowest NHIP
      Bgpv4Route lowestNhipRoute =
          Bgpv4Route.testBuilder()
              .setNetwork(prefix)
              .setNextHop(NextHopIp.of(lowestNhip))
              .setOriginMechanism(NETWORK)
              .setSrcProtocol(CONNECTED)
              .build();
      Bgpv4Route highestNhipRoute =
          Bgpv4Route.testBuilder()
              .setNetwork(prefix)
              .setNextHop(NextHopIp.of(highestNhip))
              .setOriginMechanism(NETWORK)
              .setSrcProtocol(CONNECTED)
              .build();
      Rib mainRib = new Rib();
      Bgpv4Rib bgpRib =
          new Bgpv4Rib(
              mainRib,
              BgpTieBreaker.ARRIVAL_ORDER,
              1,
              null,
              false,
              NO_PREFERENCE,
              LOWEST_NEXT_HOP_IP,
              HIGHEST_NEXT_HOP_IP, /* different on purpose */
              ResolutionRestriction.alwaysTrue());

      // Add less preferred NHIP route
      assertThat(
          bgpRib.mergeRouteGetDelta(highestNhipRoute), equalTo(RibDelta.adding(highestNhipRoute)));
      assertThat(bgpRib.getRoutes(), contains(highestNhipRoute));

      // Add more preferred NHIP route. Less preferred route should be removed, and should not
      // appear in backup.
      assertThat(
          bgpRib.mergeRouteGetDelta(lowestNhipRoute).stream()
              .collect(ImmutableList.toImmutableList()),
          containsInAnyOrder(
              RouteAdvertisement.adding(lowestNhipRoute),
              RouteAdvertisement.withdrawing(highestNhipRoute)));
      assertThat(bgpRib.getBackupRoutes(), not(hasItem(highestNhipRoute)));

      // Remove less preferred NHIP route. There should be no delta.
      assertThat(bgpRib.removeRouteGetDelta(highestNhipRoute), equalTo(RibDelta.empty()));

      // Re-add less preferred NHIP route. There should be no delta.
      assertThat(bgpRib.mergeRouteGetDelta(highestNhipRoute), equalTo(RibDelta.empty()));

      // Remove more preferred NHIP route. The less preferred one should now be added.
      assertThat(
          bgpRib.removeRouteGetDelta(lowestNhipRoute).stream()
              .collect(ImmutableList.toImmutableList()),
          containsInAnyOrder(
              RouteAdvertisement.adding(highestNhipRoute),
              RouteAdvertisement.withdrawing(lowestNhipRoute)));
    }
    {
      // Test network, preferring highest NHIP
      Bgpv4Route lowestNhipRoute =
          Bgpv4Route.testBuilder()
              .setNetwork(prefix)
              .setNextHop(NextHopIp.of(lowestNhip))
              .setOriginMechanism(NETWORK)
              .setSrcProtocol(CONNECTED)
              .build();
      Bgpv4Route highestNhipRoute =
          Bgpv4Route.testBuilder()
              .setNetwork(prefix)
              .setNextHop(NextHopIp.of(highestNhip))
              .setOriginMechanism(NETWORK)
              .setSrcProtocol(CONNECTED)
              .build();
      Rib mainRib = new Rib();
      Bgpv4Rib bgpRib =
          new Bgpv4Rib(
              mainRib,
              BgpTieBreaker.ARRIVAL_ORDER,
              1,
              null,
              false,
              NO_PREFERENCE,
              HIGHEST_NEXT_HOP_IP,
              LOWEST_NEXT_HOP_IP, /* different on purpose */
              ResolutionRestriction.alwaysTrue());

      // Add less preferred NHIP route
      assertThat(
          bgpRib.mergeRouteGetDelta(lowestNhipRoute), equalTo(RibDelta.adding(lowestNhipRoute)));
      assertThat(bgpRib.getRoutes(), contains(lowestNhipRoute));

      // Add more preferred NHIP route. Less preferred route should be removed, and should not
      // appear in backup.
      assertThat(
          bgpRib.mergeRouteGetDelta(highestNhipRoute).stream()
              .collect(ImmutableList.toImmutableList()),
          containsInAnyOrder(
              RouteAdvertisement.adding(highestNhipRoute),
              RouteAdvertisement.withdrawing(lowestNhipRoute)));
      assertThat(bgpRib.getBackupRoutes(), not(hasItem(lowestNhipRoute)));

      // Remove less preferred NHIP route. There should be no delta.
      assertThat(bgpRib.removeRouteGetDelta(lowestNhipRoute), equalTo(RibDelta.empty()));

      // Re-add less preferred NHIP route. There should be no delta.
      assertThat(bgpRib.mergeRouteGetDelta(lowestNhipRoute), equalTo(RibDelta.empty()));

      // Remove more preferred NHIP route. The less preferred one should now be added.
      assertThat(
          bgpRib.removeRouteGetDelta(highestNhipRoute).stream()
              .collect(ImmutableList.toImmutableList()),
          containsInAnyOrder(
              RouteAdvertisement.adding(lowestNhipRoute),
              RouteAdvertisement.withdrawing(highestNhipRoute)));
    }
    {
      // Test origination type tie-breaker: no preference
      Bgpv4Route networkRoute =
          Bgpv4Route.testBuilder()
              .setNetwork(prefix)
              .setNextHop(NextHopIp.of(lowestNhip))
              .setOriginMechanism(NETWORK)
              .setSrcProtocol(CONNECTED)
              .build();
      Bgpv4Route redistributeRoute =
          Bgpv4Route.testBuilder()
              .setNetwork(prefix)
              .setNextHop(NextHopIp.of(highestNhip))
              .setOriginMechanism(REDISTRIBUTE)
              .setSrcProtocol(CONNECTED)
              .build();
      Rib mainRib = new Rib();
      Bgpv4Rib bgpRib =
          new Bgpv4Rib(
              mainRib,
              BgpTieBreaker.ARRIVAL_ORDER,
              2 /* multipath */,
              EXACT_PATH,
              false,
              NO_PREFERENCE,
              HIGHEST_NEXT_HOP_IP /* same on purpose */,
              HIGHEST_NEXT_HOP_IP, /* same on purpose */
              ResolutionRestriction.alwaysTrue());

      bgpRib.mergeRouteGetDelta(networkRoute);
      bgpRib.mergeRouteGetDelta(redistributeRoute);

      // both routes should be present and equally preferred
      assertThat(bgpRib.getRoutes(), containsInAnyOrder(networkRoute, redistributeRoute));
    }
    {
      // Test origination type tie-breaker: prefer network
      Bgpv4Route networkRoute =
          Bgpv4Route.testBuilder()
              .setNetwork(prefix)
              .setNextHop(NextHopIp.of(lowestNhip))
              .setOriginMechanism(NETWORK)
              .setSrcProtocol(CONNECTED)
              .build();
      Bgpv4Route redistributeRoute =
          Bgpv4Route.testBuilder()
              .setNetwork(prefix)
              .setNextHop(NextHopIp.of(highestNhip))
              .setOriginMechanism(REDISTRIBUTE)
              .setSrcProtocol(CONNECTED)
              .build();
      Rib mainRib = new Rib();
      Bgpv4Rib bgpRib =
          new Bgpv4Rib(
              mainRib,
              BgpTieBreaker.ARRIVAL_ORDER,
              2 /* multipath */,
              EXACT_PATH,
              false,
              PREFER_NETWORK,
              HIGHEST_NEXT_HOP_IP /* same on purpose */,
              HIGHEST_NEXT_HOP_IP, /* same on purpose */
              ResolutionRestriction.alwaysTrue());

      bgpRib.mergeRouteGetDelta(networkRoute);
      bgpRib.mergeRouteGetDelta(redistributeRoute);

      // Only network route should be present.
      assertThat(bgpRib.getRoutes(), contains(networkRoute));
      // Backup should contain redistribute route.
      assertThat(bgpRib.getBackupRoutes(), containsInAnyOrder(networkRoute, redistributeRoute));
    }
    {
      // Test origination type tie-breaker: prefer redistribute
      Bgpv4Route networkRoute =
          Bgpv4Route.testBuilder()
              .setNetwork(prefix)
              .setNextHop(NextHopIp.of(lowestNhip))
              .setOriginMechanism(NETWORK)
              .setSrcProtocol(CONNECTED)
              .build();
      Bgpv4Route redistributeRoute =
          Bgpv4Route.testBuilder()
              .setNetwork(prefix)
              .setNextHop(NextHopIp.of(highestNhip))
              .setOriginMechanism(REDISTRIBUTE)
              .setSrcProtocol(CONNECTED)
              .build();
      Rib mainRib = new Rib();
      Bgpv4Rib bgpRib =
          new Bgpv4Rib(
              mainRib,
              BgpTieBreaker.ARRIVAL_ORDER,
              2 /* multipath */,
              EXACT_PATH,
              false,
              PREFER_REDISTRIBUTE,
              HIGHEST_NEXT_HOP_IP /* same on purpose */,
              HIGHEST_NEXT_HOP_IP, /* same on purpose */
              ResolutionRestriction.alwaysTrue());

      bgpRib.mergeRouteGetDelta(networkRoute);
      bgpRib.mergeRouteGetDelta(redistributeRoute);

      // Only redistribute route should be present.
      assertThat(bgpRib.getRoutes(), contains(redistributeRoute));
      // Backup should contain network route.
      assertThat(bgpRib.getBackupRoutes(), containsInAnyOrder(networkRoute, redistributeRoute));
    }
  }
}
