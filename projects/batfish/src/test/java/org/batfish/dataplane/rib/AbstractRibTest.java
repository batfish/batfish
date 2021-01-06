package org.batfish.dataplane.rib;

import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.OspfIntraAreaRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RipInternalRoute;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link AbstractRib} */
public class AbstractRibTest {
  /*
   * Test the AbstractRib tree logic. To avoid worrying about route preference comparisons, testing
   * with StaticRoutes and StaticRib. All static routes will be stored in the RIB, without eviction
   * based on preference.
   */
  private AbstractRib<StaticRoute> _rib;
  private static final StaticRoute _mostGeneralRoute =
      StaticRoute.testBuilder().setNetwork(Prefix.ZERO).setMetric(0L).setTag(0L).build();
  @Rule public ExpectedException _expectedException = ExpectedException.none();

  @Before
  public void setupEmptyRib() {
    _rib = new StaticRib();
  }

  @Test
  public void testRibConstructor() {
    // Assertions: Ensure that a new rib is empty upon construction
    assertThat(_rib.getTypedRoutes(), empty());
    assertThat(_rib.getTypedRoutes(), not(hasItem(_mostGeneralRoute)));
  }

  @Test
  public void testSingleRouteAdd() {
    // Test: Adding one route
    _rib.mergeRouteGetDelta(_mostGeneralRoute);
    assertThat(_rib.getTypedRoutes(), contains(_mostGeneralRoute));
  }

  /** Inserts overlapping routes into the RIB and returns a (manually) ordered list of them */
  private List<StaticRoute> setupOverlappingRoutes() {
    // Setup helper
    String[] testPrefixes =
        new String[] {"10.0.0.0/8", "10.0.0.0/9", "10.128.0.0/9", "10.1.1.1/32"};
    List<StaticRoute> routes = new ArrayList<>();

    // Test: merge the routes into the RIB
    StaticRoute.Builder srb = StaticRoute.testBuilder().setMetric(0L).setTag(0L);
    for (String prefixStr : testPrefixes) {
      StaticRoute r = srb.setNetwork(Prefix.parse(prefixStr)).build();
      _rib.mergeRouteGetDelta(r);
      routes.add(r);
    }
    return routes;
  }

  /** Ensure that only one copy of a route is stored, regardless of how many times we add it */
  @Test
  public void testRepeatedAdd() {
    StaticRoute route =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("10.0.0.0/11"))
            .setMetric(0L)
            .setTag(0L)
            .build();
    for (int i = 0; i < 5; i++) {
      _rib.mergeRouteGetDelta(route);
    }
    assertThat(_rib.getTypedRoutes(), contains(route));
  }

  /**
   * Check that containsRoute and route collection works as expected in the presence of
   * non-overlapping routes in the RIB
   */
  @Test
  public void testNonOverlappingRouteAdd() {
    StaticRoute.Builder srb = StaticRoute.testBuilder().setMetric(0L).setTag(0L);
    StaticRoute r1 = srb.setNetwork(Prefix.parse("1.1.1.1/32")).build();
    StaticRoute r2 = srb.setNetwork(Prefix.parse("128.1.1.1/32")).build();
    _rib.mergeRouteGetDelta(r1);
    _rib.mergeRouteGetDelta(r2);

    // Check that both routes exist
    assertThat(_rib.getTypedRoutes(), containsInAnyOrder(r1, r2));
  }

  /**
   * Check that containsRoute and route collection works as expected in the presence of overlapping
   * routes in the RIB
   */
  @Test
  public void testMultiOverlappingRouteAdd() {
    // Setup/Test: Add multiple routes with overlapping prefixes
    List<StaticRoute> routes = setupOverlappingRoutes();
    assertThat(_rib.getTypedRoutes(), containsInAnyOrder(routes.toArray()));
  }

  /** Ensure that empty RIB doesn't have any prefix matches */
  @Test
  public void testLongestPrefixMatchWhenEmpty() {
    assertThat(_rib.longestPrefixMatch(Ip.parse("1.1.1.1")), empty());
    assertThat(_rib.longestPrefixMatch(Ip.parse("0.0.0.0")), empty());
  }

  /** Ensure that longestPrefixMatch finds route in root (guarantee no off-by-one length error) */
  @Test
  public void testLongestPrefixMatchWhenInRoot() {
    StaticRoute r =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("0.0.0.0/0"))
            .setAdministrativeCost(1)
            .build();
    _rib.mergeRouteGetDelta(r);
    assertThat(_rib.longestPrefixMatch(Ip.parse("1.1.1.1"), 32), contains(r));
  }

  /**
   * Ensure that {@link AbstractRib#longestPrefixMatch(Ip)} returns correct routes when the RIB is
   * non-empty
   */
  @Test
  public void testLongestPrefixMatch() {
    List<StaticRoute> routes = setupOverlappingRoutes();

    Set<StaticRoute> match = _rib.longestPrefixMatch(Ip.parse("10.1.1.1"));
    assertThat(match, contains(routes.get(3)));

    match = _rib.longestPrefixMatch(Ip.parse("10.1.1.2"));
    assertThat(match, contains(routes.get(1)));

    match = _rib.longestPrefixMatch(Ip.parse("11.1.1.1"));
    assertThat(match, empty());
  }

  /**
   * Ensure that {@link AbstractRib#longestPrefixMatch(Ip, int)} returns correct routes when the RIB
   * is non-empty
   */
  @Test
  public void testLongestPrefixMatchConstrained() {
    List<StaticRoute> routes = setupOverlappingRoutes();

    // Only the first route matches with prefix len of <= 8
    Set<StaticRoute> match = _rib.longestPrefixMatch(Ip.parse("10.1.1.1"), 8);
    assertThat(match, contains(routes.get(0)));
  }

  /** Test equality across different RIB instances */
  @Test
  public void testHasSameRoutes() {
    // And create a new different RIB
    AbstractRib<StaticRoute> rib2 = new StaticRib();
    assertThat(rib2, equalTo(_rib));

    List<StaticRoute> routes = setupOverlappingRoutes();
    assertThat(rib2, not(equalTo(_rib)));

    // Add routes
    rib2.mergeRouteGetDelta(routes.get(0));
    rib2.mergeRouteGetDelta(routes.get(2));
    rib2.mergeRouteGetDelta(routes.get(3));
    assertThat(rib2, not(equalTo(_rib)));

    rib2.mergeRouteGetDelta(routes.get(1));
    assertThat(rib2, equalTo(_rib));
  }

  /**
   * Check that getRoutes works as expected even when routes replace other routes based on
   * preference
   */
  @Test
  public void testGetRoutesWithReplacement() {
    // Use OSPF RIBs for this, as routes with better metric can replace other routes
    OspfIntraAreaRib rib = new OspfIntraAreaRib();
    Prefix prefix = Prefix.parse("1.1.1.1/32");
    OspfIntraAreaRoute ospfRoute =
        OspfIntraAreaRoute.builder()
            .setNetwork(prefix)
            .setNextHop(NextHopDiscard.instance())
            .setAdmin(100)
            .setMetric(30)
            .setArea(1L)
            .build();

    rib.mergeRouteGetDelta(ospfRoute);
    assertThat(rib.getRoutes(), hasSize(1));

    // This new route replaces old route
    OspfIntraAreaRoute newRoute = ospfRoute.toBuilder().setMetric(10).build();
    rib.mergeRouteGetDelta(newRoute);
    assertThat(rib.getRoutes(), contains(newRoute));

    // Add completely new route and check that the size increases
    rib.mergeRouteGetDelta(ospfRoute.toBuilder().setNetwork(Prefix.parse("2.2.2.2/32")).build());
    assertThat(rib.getRoutes(), hasSize(2));
  }

  /** Test that routes obtained from getTypedRoutes() cannot be modified */
  @Test
  public void testGetRoutesCannotBeModified() {
    _rib.mergeRouteGetDelta(_mostGeneralRoute);
    Set<StaticRoute> routes = _rib.getTypedRoutes();
    StaticRoute r1 =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setMetric(0L)
            .setTag(0L)
            .build();

    // Exception because ImmutableSet
    _expectedException.expect(UnsupportedOperationException.class);
    routes.add(r1);
  }

  /**
   * Test that routes obtained from getTypedRoutes() do NOT reflect subsequent changes to the RIB
   */
  @Test
  public void testGetRoutesIsNotAView() {
    _rib.mergeRouteGetDelta(_mostGeneralRoute);
    Set<StaticRoute> routes = _rib.getTypedRoutes();
    StaticRoute r1 =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setMetric(0L)
            .setTag(0L)
            .build();

    _rib.mergeRouteGetDelta(r1);

    assertThat(routes, not(hasItem(r1)));
  }

  /**
   * Test that multiple calls to getTypedRoutes() return the same object, if the RIB has not been
   * modified
   */
  @Test
  public void testGetRoutesSameObject() {
    _rib.mergeRouteGetDelta(_mostGeneralRoute);

    Set<StaticRoute> routes = _rib.getTypedRoutes();
    assertThat(_rib.getTypedRoutes(), sameInstance(routes));
  }

  /** Test that correct delta is returned when adding a new route. */
  @Test
  public void testAddRouteGetDelta() {
    AbstractRib<RipInternalRoute> rib = new RipInternalRib();
    int admin = RoutingProtocol.RIP.getDefaultAdministrativeCost(ConfigurationFormat.CISCO_IOS);
    Prefix prefix = Prefix.create(Ip.parse("10.0.0.0"), 8);
    // High metric
    RipInternalRoute oldRoute =
        RipInternalRoute.builder()
            .setNetwork(prefix)
            .setNextHop(NextHopDiscard.instance())
            .setAdmin(admin)
            .setMetric(10)
            .build();
    // New route, lower metric, will override oldRoute
    RipInternalRoute newRoute =
        RipInternalRoute.builder()
            .setNetwork(prefix)
            .setNextHop(NextHopDiscard.instance())
            .setAdmin(admin)
            .setMetric(5)
            .build();

    // First merge old route
    RibDelta<RipInternalRoute> delta = rib.mergeRouteGetDelta(oldRoute);
    List<RouteAdvertisement<RipInternalRoute>> actions =
        delta.getActions().collect(Collectors.toList());
    assertThat(actions, contains(new RouteAdvertisement<>(oldRoute)));
    assertThat(delta.getRoutes(), hasItem(oldRoute));

    // Try re-merging, should get empty delta, because RIB has not changed
    delta = rib.mergeRouteGetDelta(oldRoute);
    actions = delta.getActions().collect(Collectors.toList());
    assertThat(actions, empty());

    // Now replace with a newer route, check that one route removed, one added
    delta = rib.mergeRouteGetDelta(newRoute);
    assertThat(delta.getRoutes(), hasItem(oldRoute));
    assertThat(delta.getRoutes(), hasItem(newRoute));
  }

  /**
   * Test that {@link AbstractRib#removeRoute} actually removes the route and returns correct {@link
   * RibDelta}
   */
  @Test
  public void testRemoveRoute() {
    StaticRoute r =
        StaticRoute.testBuilder()
            .setNetwork(Ip.parse("1.1.1.1").toPrefix())
            .setMetric(0L)
            .setTag(1L)
            .build();
    _rib.mergeRoute(_mostGeneralRoute);
    _rib.mergeRoute(r);

    // Remove
    RibDelta<StaticRoute> d = _rib.removeRouteGetDelta(_mostGeneralRoute);
    List<RouteAdvertisement<StaticRoute>> actions = d.getActions().collect(Collectors.toList());

    // Check only route r remains
    assertThat(_rib.getTypedRoutes(), contains(r));
    assertThat(actions, contains(new RouteAdvertisement<>(_mostGeneralRoute, Reason.WITHDRAW)));

    // Remove route r
    d = _rib.removeRouteGetDelta(r);
    actions = d.getActions().collect(Collectors.toList());
    assertThat(_rib.getTypedRoutes(), empty());
    assertThat(actions, contains(new RouteAdvertisement<>(r, Reason.WITHDRAW)));
  }

  /**
   * Test that {@link AbstractRib#removeRoute} correctly removes the route and returns proper delta
   * if multiple routes have the same preference, but only one of them is getting removed
   */
  @Test
  public void testRemoveRouteSamePreference() {
    // Two routes for same prefix,
    StaticRoute r1 =
        StaticRoute.testBuilder()
            .setNetwork(Ip.parse("1.1.1.1").toPrefix())
            .setAdministrativeCost(1)
            .setMetric(0L)
            .setTag(1L)
            .build();
    StaticRoute r2 =
        StaticRoute.testBuilder()
            .setNetwork(Ip.parse("1.1.1.1").toPrefix())
            .setNextHop(NextHopIp.of(Ip.parse("2.2.2.2")))
            .setAdministrativeCost(1)
            .setMetric(0L)
            .setTag(1L)
            .build();

    _rib.mergeRoute(r1);
    _rib.mergeRoute(r2);
    // sanity check here
    assertThat(_rib.getRoutes(), hasSize(2));

    // Remove only r1, check that r2 remains
    _rib.removeRoute(r1);
    assertThat(_rib.getTypedRoutes(), contains(r2));
  }

  @Test
  public void testLengthLimit() {
    StaticRoute.Builder builder =
        StaticRoute.testBuilder().setAdministrativeCost(1).setMetric(0L).setTag(1L);

    Ip ip = Ip.parse("1.1.1.1");
    StaticRoute r32 = builder.setNetwork(ip.toPrefix()).build();
    StaticRoute r18 = builder.setNetwork(Prefix.create(ip, 18)).build();
    _rib.mergeRoute(r32);
    _rib.mergeRoute(r18);

    assertThat(_rib.longestPrefixMatch(ip, 32), contains(r32));
    assertThat(_rib.longestPrefixMatch(ip, 31), contains(r18));
    assertThat(_rib.longestPrefixMatch(ip, 19), contains(r18));
    assertThat(_rib.longestPrefixMatch(ip, 18), contains(r18));
    assertThat(_rib.longestPrefixMatch(ip, 17), empty());
  }

  @Test
  public void testNonForwarding() {
    StaticRoute.Builder b = StaticRoute.testBuilder().setMetric(0L).setTag(0L);
    // Non forwarding 1.2.3.4/32
    _rib.mergeRouteGetDelta(
        b.setNetwork(Prefix.parse("1.2.3.4/32")).setNonForwarding(true).build());
    // Non forwarding 1.2.3.4/31
    _rib.mergeRouteGetDelta(
        b.setNetwork(Prefix.parse("1.2.3.4/31")).setNonForwarding(true).build());
    // Two routes, one forwarding one non-forwarding for 1.2.3.4/30.
    _rib.mergeRouteGetDelta(
        b.setNetwork(Prefix.parse("1.2.3.4/30")).setNonForwarding(true).build());
    _rib.mergeRouteGetDelta(
        b.setNetwork(Prefix.parse("1.2.3.4/30")).setNonForwarding(false).build());
    _rib.mergeRouteGetDelta(
        b.setNetwork(Prefix.parse("1.2.3.4/8")).setNonForwarding(false).build());

    // Looking for 1.2.3.4, should skip the /32 and /31, and then find the single forwarding /30
    Set<StaticRoute> routes = _rib.longestPrefixMatch(Ip.parse("1.2.3.4"));
    assertThat(routes, contains(hasPrefix(Prefix.parse("1.2.3.4/30"))));
  }

  @Test
  public void testClear() {
    // Setup: just merge in a bunch of routes
    StaticRoute.Builder b = StaticRoute.testBuilder().setMetric(0L).setTag(0L);
    _rib.mergeRouteGetDelta(
        b.setNetwork(Prefix.parse("1.2.3.4/32")).setNonForwarding(true).build());
    _rib.mergeRouteGetDelta(
        b.setNetwork(Prefix.parse("1.2.3.4/31")).setNonForwarding(true).build());
    _rib.mergeRouteGetDelta(
        b.setNetwork(Prefix.parse("1.2.3.4/30")).setNonForwarding(true).build());
    _rib.mergeRouteGetDelta(
        b.setNetwork(Prefix.parse("1.2.3.4/30")).setNonForwarding(false).build());
    _rib.mergeRouteGetDelta(
        b.setNetwork(Prefix.parse("1.2.3.4/8")).setNonForwarding(false).build());

    // Test:
    _rib.clear();
    assertThat(_rib.getRoutes(), empty());
    assertThat(_rib.getTypedRoutes(), empty());
  }

  @Test
  public void testBackupLogic() {
    // Use concrete BGP rib, BGP has easy preference knobs
    Bgpv4Rib bestPathRib =
        new Bgpv4Rib(
            null,
            BgpTieBreaker.ROUTER_ID,
            1,
            MultipathEquivalentAsPathMatchMode.EXACT_PATH,
            true,
            false);
    Ip originator1 = Ip.parse("1.1.1.1");
    Ip originator2 = Ip.parse("2.2.2.2");
    Bgpv4Route.Builder routeBuilder =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.ZERO)
            .setLocalPreference(100)
            .setOriginType(OriginType.INCOMPLETE)
            .setOriginatorIp(originator1)
            .setProtocol(RoutingProtocol.IBGP)
            .setReceivedFromIp(originator1);

    Bgpv4Route route1 = routeBuilder.build();
    Bgpv4Route route2 =
        routeBuilder
            .setLocalPreference(2000)
            .setOriginatorIp(originator2)
            .setReceivedFromIp(originator2)
            .build();
    Bgpv4Route route3 = routeBuilder.setLocalPreference(1).build();

    bestPathRib.mergeRoute(route1);
    bestPathRib.mergeRoute(route2);
    bestPathRib.mergeRoute(route3);
    // Route 2 is preferred so it replaces route 1
    assertThat(bestPathRib.getTypedRoutes(), contains(route2));
    RibDelta<Bgpv4Route> delta = bestPathRib.removeRouteGetDelta(route2);
    // Route 2 is removed but route 1 fills the gap as the next-best route
    assertThat(bestPathRib.getTypedRoutes(), contains(route1));
    assertThat(
        delta.getActions().collect(Collectors.toList()),
        contains(
            new RouteAdvertisement<>(route2, Reason.WITHDRAW), new RouteAdvertisement<>(route1)));
  }
}
