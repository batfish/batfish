package org.batfish.dataplane.rib;

import static org.batfish.dataplane.ibdp.TestUtils.annotateRoute;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopIp;
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
        .setReceivedFromIp(Ip.parse("1.1.1.1"))
        .setSrcProtocol(RoutingProtocol.CONNECTED)
        .setWeight(0);

    _multiPathRib =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ROUTER_ID,
            null,
            MultipathEquivalentAsPathMatchMode.EXACT_PATH,
            false,
            false);
    _bestPathRib = new Bgpv4Rib(null, BgpTieBreaker.ROUTER_ID, 1, null, false, false);
  }

  @Test
  public void testParameterValidationMaxPaths() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Invalid max-paths value");
    new Bgpv4Rib(null, BgpTieBreaker.ARRIVAL_ORDER, 0, null, false, false);
  }

  @Test
  public void testParameterValidationMatchMode() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Multipath AS-Path-Match-mode must be specified");
    new Bgpv4Rib(null, BgpTieBreaker.ARRIVAL_ORDER, 2, null, false, false);
  }

  @Test
  public void testParameterValidationMatchModeNullMaxPaths() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Multipath AS-Path-Match-mode must be specified");
    new Bgpv4Rib(null, BgpTieBreaker.ARRIVAL_ORDER, null, null, false, false);
  }

  @Test
  public void testIsMultipath() {
    Bgpv4Rib rib = new Bgpv4Rib(null, BgpTieBreaker.ARRIVAL_ORDER, 1, null, false, false);
    assertTrue("MaxPaths=1, not multipath", !rib.isMultipath());
    rib =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ARRIVAL_ORDER,
            2,
            MultipathEquivalentAsPathMatchMode.EXACT_PATH,
            false,
            false);
    assertTrue("Maxpaths=2 -> multipath", rib.isMultipath());
    rib =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ARRIVAL_ORDER,
            null,
            MultipathEquivalentAsPathMatchMode.EXACT_PATH,
            false,
            false);
    assertTrue("Maxpaths=null -> multipath", rib.isMultipath());
  }

  @Test
  public void testWeightPreference() {
    Bgpv4Route worse = _rb.setWeight(0).build();
    // Higher value is preferred
    Bgpv4Route best = _rb.setWeight(1).build();
    _multiPathRib.mergeRoute(worse);
    _multiPathRib.mergeRoute(best);

    assertThat(_multiPathRib.getRoutes(), contains(best));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(best));
  }

  @Test
  public void testLocalPreference() {
    Bgpv4Route worse = _rb.setLocalPreference(100).build();
    // Higher value is preferred
    Bgpv4Route best = _rb.setLocalPreference(200).build();
    _multiPathRib.mergeRoute(worse);
    _multiPathRib.mergeRoute(best);

    assertThat(_multiPathRib.getRoutes(), contains(best));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(best));
  }

  @Test
  public void testAggregatePreference() {
    Bgpv4Route worse = _rb.setProtocol(RoutingProtocol.BGP).build();
    Bgpv4Route best = _rb.setProtocol(RoutingProtocol.AGGREGATE).build();
    _multiPathRib.mergeRoute(worse);
    _multiPathRib.mergeRoute(best);

    assertThat(_multiPathRib.getRoutes(), contains(best));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(best));
  }

  @Test
  public void testAsPathLengthPreference() {
    Bgpv4Route worse = _rb.setAsPath(AsPath.ofSingletonAsSets(1L, 2L)).build();
    // shorter path is preferred
    Bgpv4Route best = _rb.setAsPath(AsPath.ofSingletonAsSets(3L)).build();
    _multiPathRib.mergeRoute(worse);
    _multiPathRib.mergeRoute(best);

    assertThat(_multiPathRib.getRoutes(), contains(best));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(best));
  }

  @Test
  public void testOriginTypePreference() {
    Bgpv4Route worst = _rb.setOriginType(OriginType.INCOMPLETE).build();
    Bgpv4Route medium = _rb.setOriginType(OriginType.EGP).build();
    Bgpv4Route best = _rb.setOriginType(OriginType.IGP).build();

    _multiPathRib.mergeRoute(worst);
    _multiPathRib.mergeRoute(medium);

    assertThat(_multiPathRib.getRoutes(), contains(medium));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(medium));

    _multiPathRib.mergeRoute(best);
    assertThat(_multiPathRib.getRoutes(), contains(best));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(best));
  }

  @Test
  public void testEIbgpProtocolPreference() {
    Bgpv4Route worse = _rb.setProtocol(RoutingProtocol.IBGP).build();
    Bgpv4Route best = _rb.setProtocol(RoutingProtocol.BGP).build();

    _multiPathRib.mergeRoute(worse);
    _multiPathRib.mergeRoute(best);

    assertThat(_multiPathRib.getRoutes(), contains(best));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(best));
  }

  @Test
  public void testMedPreference() {
    Bgpv4Route worse = _rb.setMetric(1).build();
    Bgpv4Route best = _rb.setMetric(0).build();

    _multiPathRib.mergeRoute(worse);
    _multiPathRib.mergeRoute(best);

    assertThat(_multiPathRib.getRoutes(), contains(best));
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
            MultipathEquivalentAsPathMatchMode.EXACT_PATH,
            false,
            false);

    Bgpv4Route worse = _rb.setNextHopIp(Ip.parse("5.5.5.6")).build();
    // Lower IGP cost to next hop is better
    Bgpv4Route best = _rb.setNextHopIp(Ip.parse("5.5.5.5")).build();

    rib.mergeRoute(worse);
    rib.mergeRoute(best);
    assertThat(rib.getRoutes(), contains(best));
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
            MultipathEquivalentAsPathMatchMode.EXACT_PATH,
            false,
            false);

    // Discard next hop is worse despite lower IGP cost
    Bgpv4Route worse = _rb.setNextHopIp(Ip.parse("5.5.5.6")).build();
    Bgpv4Route best = _rb.setNextHopIp(Ip.parse("5.5.5.5")).build();

    rib.mergeRoute(worse);
    rib.mergeRoute(best);
    assertThat(rib.getRoutes(), contains(best));
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
            MultipathEquivalentAsPathMatchMode.EXACT_PATH,
            false,
            false);

    Bgpv4Route worse =
        _rb.setNetwork(Prefix.strict("6.0.0.0/24")).setNextHopIp(Ip.parse("4.4.4.6")).build();
    // Lower IGP cost to next hop is better
    Bgpv4Route best =
        _rb.setNetwork(Prefix.strict("6.0.0.0/24")).setNextHopIp(Ip.parse("4.4.4.5")).build();

    rib.mergeRoute(worse);
    rib.mergeRoute(best);
    assertThat(rib.getRoutes(), contains(best));
    assertThat(rib.getBestPathRoutes(), contains(best));
  }

  @Test
  public void testMultipathAsPathModeExactPath() {
    Bgpv4Rib rib =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ROUTER_ID,
            null,
            MultipathEquivalentAsPathMatchMode.EXACT_PATH,
            false,
            false);

    Bgpv4Route base = _rb.setAsPath(AsPath.ofSingletonAsSets(1L, 2L)).build();
    Bgpv4Route candidate1 =
        _rb.setAsPath(AsPath.ofSingletonAsSets(1L, 2L)).setNextHopIp(Ip.parse("5.5.5.5")).build();
    Bgpv4Route candidate2 =
        _rb.setAsPath(AsPath.ofSingletonAsSets(1L, 3L)).setNextHopIp(Ip.parse("5.5.5.6")).build();

    rib.mergeRoute(base);
    assertTrue("Exact AS path match, allow merge", rib.mergeRoute(candidate1));
    assertFalse("Not an exact AS path match, don't merge", rib.mergeRoute(candidate2));
    assertThat(rib.getRoutes(), hasSize(2));
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
            false);

    Bgpv4Route base = _rb.setAsPath(AsPath.ofSingletonAsSets(1L, 2L)).build();
    Bgpv4Route candidate1 =
        _rb.setAsPath(AsPath.ofSingletonAsSets(1L, 3L)).setNextHopIp(Ip.parse("5.5.5.5")).build();
    Bgpv4Route candidate2 =
        _rb.setAsPath(AsPath.ofSingletonAsSets(2L, 3L)).setNextHopIp(Ip.parse("5.5.5.6")).build();

    rib.mergeRoute(base);
    assertTrue("Exact AS path match, allow merge", rib.mergeRoute(candidate1));
    assertTrue("Not an exact AS path match, don't merge", !rib.mergeRoute(candidate2));
    assertThat(rib.getRoutes(), hasSize(2));
    assertThat(rib.getBestPathRoutes(), hasSize(1));
  }

  @Test
  public void testMultipathDiffOriginator() {
    Bgpv4Route bestPath = _rb.build();
    _multiPathRib.mergeRoute(_rb.setOriginatorIp(Ip.parse("2.2.2.2")).build());
    _multiPathRib.mergeRoute(_rb.setOriginatorIp(Ip.parse("2.2.2.3")).build());
    _multiPathRib.mergeRoute(bestPath);
    assertThat(_multiPathRib.getRoutes(), hasSize(3));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(bestPath));
  }

  @Test
  public void testMultipathArrivalOrder() {
    _multiPathRib =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ARRIVAL_ORDER,
            null,
            MultipathEquivalentAsPathMatchMode.EXACT_PATH,
            false,
            false);
    Bgpv4Route best = _rb.build();
    Bgpv4Route earliest = _rb.setOriginatorIp(Ip.parse("2.2.2.2")).build();
    _multiPathRib.mergeRoute(earliest);
    _multiPathRib.mergeRoute(_rb.setOriginatorIp(Ip.parse("2.2.2.3")).build());
    _multiPathRib.mergeRoute(best);

    assertThat(_multiPathRib.getRoutes(), hasSize(3));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(earliest));
  }

  @Test
  public void testMultipathDiffClusterList() {
    _rb.setProtocol(RoutingProtocol.IBGP).setClusterList(ImmutableSortedSet.of());
    Bgpv4Route bestPath = _rb.build();
    _multiPathRib.mergeRoute(bestPath);
    _multiPathRib.mergeRoute(_rb.setClusterList(ImmutableSortedSet.of(11L)).build());
    _multiPathRib.mergeRoute(_rb.setClusterList(ImmutableSortedSet.of(22L, 33L)).build());

    assertThat(_multiPathRib.getRoutes(), hasSize(3));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(bestPath));
  }

  @Test
  public void testMultipathDiffNeighbor() {
    Bgpv4Route bestPath = _rb.build();
    _multiPathRib.mergeRoute(_rb.setReceivedFromIp(Ip.parse("2.2.2.2")).build());
    _multiPathRib.mergeRoute(_rb.setReceivedFromIp(Ip.parse("2.2.2.3")).build());
    _multiPathRib.mergeRoute(bestPath);

    assertThat(_multiPathRib.getRoutes(), hasSize(3));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(bestPath));
  }

  @Test
  public void testMultipathSamePath() {
    Bgpv4Route bestPath = _rb.build();
    _multiPathRib.mergeRoute(bestPath);
    _multiPathRib.mergeRoute(bestPath);
    _multiPathRib.mergeRoute(bestPath);

    assertThat(_multiPathRib.getRoutes(), hasSize(1));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(bestPath));
  }

  @Test
  public void testMultipathEviction() {
    _multiPathRib.mergeRoute(_rb.setOriginatorIp(Ip.parse("4.4.4.4")).build());
    _multiPathRib.mergeRoute(_rb.setReceivedFromIp(Ip.parse("2.2.2.2")).build());
    _multiPathRib.mergeRoute(_rb.setReceivedFromIp(Ip.parse("2.2.2.3")).build());

    assertThat(_multiPathRib.getRoutes(), hasSize(3));
    assertThat(_multiPathRib.getBestPathRoutes(), hasSize(1));
    Bgpv4Route bestPath = _rb.setLocalPreference(1000).build();
    _multiPathRib.mergeRoute(bestPath);

    assertThat(_multiPathRib.getRoutes(), contains(bestPath));
    assertThat(_multiPathRib.getBestPathRoutes(), contains(bestPath));
  }

  @Test
  public void testBestPathsEqualRoutesForBestPathRib() {
    Bgpv4Rib bestPathRib = new Bgpv4Rib(null, BgpTieBreaker.ROUTER_ID, 1, null, false, false);
    Bgpv4Route bestPath = _rb.build();
    bestPathRib.mergeRoute(_rb.setReceivedFromIp(Ip.parse("2.2.2.2")).build());
    bestPathRib.mergeRoute(_rb.setReceivedFromIp(Ip.parse("2.2.2.3")).build());
    bestPathRib.mergeRoute(bestPath);

    assertThat(bestPathRib.getRoutes(), contains(bestPath));
    assertThat(bestPathRib.getBestPathRoutes(), contains(bestPath));
  }

  @Test
  public void testMergeMultiplePrefixes() {
    _multiPathRib.mergeRoute(_rb.build());
    _multiPathRib.mergeRoute(_rb.setNetwork(Prefix.parse("10.1.0.0/16")).build());
    _multiPathRib.mergeRoute(_rb.setNetwork(Prefix.parse("10.1.1.0/24")).build());

    assertThat(_multiPathRib.getRoutes(), hasSize(3));
    assertThat(_multiPathRib.getRoutes(), equalTo(_multiPathRib.getBestPathRoutes()));

    _multiPathRib.mergeRoute(
        _rb.setNetwork(Prefix.parse("10.1.1.0/24"))
            .setOriginatorIp(Ip.parse("22.22.22.22"))
            .build());
    assertThat(_multiPathRib.getRoutes(), hasSize(4));
    assertThat(_multiPathRib.getBestPathRoutes(), hasSize(3));
  }

  @Test
  public void testBestPathSelectionWithoutTieBreaking() {
    _bestPathRib.mergeRoute(_rb.build());
    Bgpv4Route bestPath = _rb.setLocalPreference(200).build();
    _bestPathRib.mergeRoute(bestPath);

    assertThat(_bestPathRib.getRoutes(), contains(bestPath));
  }

  @Test
  public void testBestPathSelectionTieBreakRouterId() {
    _bestPathRib.mergeRoute(_rb.build());
    // Lower originator IP is better
    Bgpv4Route bestPath = _rb.setOriginatorIp(Ip.parse("1.1.0.1")).build();
    _bestPathRib.mergeRoute(bestPath);

    assertThat(_bestPathRib.getRoutes(), contains(bestPath));
  }

  @Test
  public void testBestPathSelectionTieBreakReceivedFrom() {
    _bestPathRib.mergeRoute(_rb.build());
    // Lower IP is better
    Bgpv4Route bestPath = _rb.setReceivedFromIp(Ip.parse("1.1.0.1")).build();
    _bestPathRib.mergeRoute(bestPath);

    assertThat(_bestPathRib.getRoutes(), contains(bestPath));
  }

  @Test
  public void testBestPathSelectionTieBreakingArrivalOrder() {
    _bestPathRib = new Bgpv4Rib(null, BgpTieBreaker.ARRIVAL_ORDER, 1, null, false, false);
    Bgpv4Route bestPath = _rb.build();
    _bestPathRib.mergeRoute(bestPath);
    // Oldest route should win despite newer having lower in Originator IP
    _bestPathRib.mergeRoute(_rb.setOriginatorIp(Ip.parse("1.1.0.1")).build());

    assertThat(_bestPathRib.getRoutes(), contains(bestPath));
  }

  @Test
  public void testBestPathSelectionTieBreakingEbgpOnly() {
    _bestPathRib = new Bgpv4Rib(null, BgpTieBreaker.ARRIVAL_ORDER, 1, null, false, false);
    Bgpv4Route bestPath =
        _rb.setProtocol(RoutingProtocol.IBGP).setClusterList(ImmutableSet.of()).build();
    Bgpv4Route earliestPath =
        _rb.setProtocol(RoutingProtocol.IBGP).setClusterList(ImmutableSet.of(3L)).build();
    // ibgp should not care about arrival order
    _bestPathRib.mergeRoute(earliestPath);
    _bestPathRib.mergeRoute(bestPath);

    assertThat(_bestPathRib.getRoutes(), contains(bestPath));
  }

  @Test
  public void testBestPathSelectionClusterListLengthTrue() {
    _bestPathRib = new Bgpv4Rib(null, BgpTieBreaker.ARRIVAL_ORDER, 1, null, false, true);
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

    assertThat(_bestPathRib.getRoutes(), contains(bestPath));
  }

  @Test
  public void testBestPathSelectionClusterListLengthFalse() {
    _bestPathRib = new Bgpv4Rib(null, BgpTieBreaker.ARRIVAL_ORDER, 1, null, false, false);
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

    assertThat(_bestPathRib.getRoutes(), contains(bestPath));
  }

  /** We should not merge routes for which next hop is unreachable */
  @Test
  public void testRejectNextHopUnreachable() {
    Rib mainRib = new Rib();
    Bgpv4Rib bgpRib = new Bgpv4Rib(mainRib, BgpTieBreaker.ARRIVAL_ORDER, 1, null, false, false);
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
    Bgpv4Rib bgpRib = new Bgpv4Rib(mainRib, BgpTieBreaker.ARRIVAL_ORDER, 1, null, false, false);
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
            .setReceivedFromIp(Ip.parse("1.1.1.1."))
            .build();

    /*
     * Instantiate routes
     */
    Bgpv4Route route2 =
        b.setAsPath(asPath2)
            .setNextHopIp(nextHop2)
            .setOriginatorIp(nextHop2)
            .setReceivedFromIp(nextHop2)
            .build();
    Bgpv4Route route3a =
        b.setAsPath(asPath3a)
            .setNextHopIp(nextHop3a)
            .setOriginatorIp(nextHop3a)
            .setReceivedFromIp(nextHop3a)
            .build();
    Bgpv4Route route3b =
        b.setAsPath(asPath3b)
            .setNextHopIp(nextHop3b)
            .setOriginatorIp(nextHop3b)
            .setReceivedFromIp(nextHop3b)
            .build();
    Bgpv4Route route3c =
        b.setAsPath(asPath3c)
            .setNextHopIp(nextHop3c)
            .setOriginatorIp(nextHop3c)
            .setReceivedFromIp(nextHop3c)
            .build();
    Bgpv4Route route3d =
        b.setAsPath(asPath3d)
            .setNextHopIp(nextHop3d)
            .setOriginatorIp(nextHop3d)
            .setReceivedFromIp(nextHop3d)
            .build();

    Bgpv4Rib bmr =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ARRIVAL_ORDER,
            null,
            multipathEquivalentAsPathMatchMode,
            false,
            false);

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
    Set<Bgpv4Route> postMergeRoutes = bmr.getTypedRoutes();
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
    testBgpAsPathMultipathHelper(
        MultipathEquivalentAsPathMatchMode.EXACT_PATH, false, false, false, true, true);
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
            MultipathEquivalentAsPathMatchMode.EXACT_PATH,
            false,
            false);
    Bgpv4Rib bmr =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ROUTER_ID,
            null,
            MultipathEquivalentAsPathMatchMode.EXACT_PATH,
            false,
            false);

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
          b.setOriginatorIp(Ip.ZERO).setReceivedFromIp(Ip.ZERO).setOriginType(originType).build());
      routes.add(
          b.setOriginatorIp(Ip.MAX).setReceivedFromIp(Ip.MAX).setOriginType(originType).build());
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
            MultipathEquivalentAsPathMatchMode.EXACT_PATH,
            false,
            false);
    Bgpv4Rib bmr =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ROUTER_ID,
            null,
            MultipathEquivalentAsPathMatchMode.EXACT_PATH,
            false,
            false);
    Ip ip1 = Ip.parse("1.0.0.0");
    Ip ip2 = Ip.parse("2.2.0.0");
    Bgpv4Route.Builder b1 =
        Bgpv4Route.testBuilder()
            .setNextHop(NextHopIp.of(Ip.parse("3.3.3.3")))
            .setOriginType(OriginType.INCOMPLETE)
            .setOriginatorIp(Ip.ZERO)
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFromIp(Ip.ZERO);
    Bgpv4Route.Builder b2 =
        Bgpv4Route.testBuilder()
            .setNextHop(NextHopIp.of(Ip.parse("3.3.3.3")))
            .setOriginType(OriginType.INCOMPLETE)
            .setOriginatorIp(Ip.MAX)
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFromIp(Ip.ZERO);

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
            MultipathEquivalentAsPathMatchMode.EXACT_PATH,
            false,
            false);
    // ebgp
    Bgpv4Rib ebgpBpr =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ROUTER_ID,
            1,
            MultipathEquivalentAsPathMatchMode.EXACT_PATH,
            false,
            false);
    Bgpv4Route.Builder ebgpBuilder =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.ZERO)
            .setOriginType(OriginType.INCOMPLETE)
            .setOriginatorIp(Ip.ZERO)
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFromIp(Ip.ZERO);
    Bgpv4Route ebgpOlderHigherOriginator =
        ebgpBuilder.setOriginatorIp(Ip.MAX).setReceivedFromIp(Ip.parse("1.1.1.1")).build();
    Bgpv4Route ebgpNewerHigherOriginator =
        ebgpBuilder.setOriginatorIp(Ip.MAX).setReceivedFromIp(Ip.parse("1.1.1.2")).build();
    Bgpv4Route ebgpLowerOriginator = ebgpBuilder.setOriginatorIp(Ip.ZERO).build();
    // ibgp
    Bgpv4Rib ibgpBpr =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ROUTER_ID,
            1,
            MultipathEquivalentAsPathMatchMode.EXACT_PATH,
            false,
            false);
    Bgpv4Route.Builder ibgpBuilder =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.ZERO)
            .setOriginType(OriginType.INCOMPLETE)
            .setOriginatorIp(Ip.ZERO)
            .setProtocol(RoutingProtocol.IBGP)
            .setReceivedFromIp(Ip.ZERO);
    Bgpv4Route ibgpOlderHigherOriginator =
        ibgpBuilder.setOriginatorIp(Ip.MAX).setReceivedFromIp(Ip.parse("1.1.1.1")).build();
    Bgpv4Route ibgpNewerHigherOriginator =
        ibgpBuilder.setOriginatorIp(Ip.MAX).setReceivedFromIp(Ip.parse("1.1.1.2")).build();
    Bgpv4Route ibgpLowerOriginator = ibgpBuilder.setOriginatorIp(Ip.ZERO).build();

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
}
