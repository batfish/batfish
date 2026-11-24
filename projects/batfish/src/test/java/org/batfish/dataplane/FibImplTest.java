package org.batfish.dataplane;

import static org.batfish.datamodel.ResolutionRestriction.alwaysTrue;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.FibActionMatchers.hasInterfaceName;
import static org.batfish.datamodel.matchers.FibActionMatchers.isFibForwardActionThat;
import static org.batfish.datamodel.matchers.FibEntryMatchers.hasAction;
import static org.batfish.dataplane.ibdp.TestUtils.annotateRoute;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FibEntry;
import org.batfish.datamodel.FibForward;
import org.batfish.datamodel.FibNextVrf;
import org.batfish.datamodel.FibNullRoute;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.MockRib;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ResolutionRestriction;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.dataplane.rib.Rib;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests of {@link FibImpl} */
final class FibImplTest {
  @TempDir public File folder;

  private static final Ip DST_IP = Ip.parse("3.3.3.3");
  private static final String NODE1 = "node1";
  private static final String FAST_ETHERNET_0 = "FastEthernet0/0";
  private static final ConcreteInterfaceAddress NODE1_PHYSICAL_NETWORK =
      ConcreteInterfaceAddress.parse("2.0.0.1/8");
  private static final Ip EXTERNAL_IP = Ip.parse("7.7.7.7");

  private Interface.Builder _ib;
  private Configuration _config;
  private Vrf _vrf;

  @BeforeEach
  void setup() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _ib = nf.interfaceBuilder();
    _config = cb.setHostname(NODE1).build();
    _vrf = nf.vrfBuilder().setOwner(_config).setName(Configuration.DEFAULT_VRF_NAME).build();
    _ib.setOwner(_config).setVrf(_vrf);
  }

  private static Set<AbstractRoute> getTopLevelRoutesByInterface(Fib fib, String ifaceName) {
    return fib.allEntries().stream()
        .filter(e -> ((FibForward) e.getAction()).getInterfaceName().equals(ifaceName))
        .map(FibEntry::getTopLevelRoute)
        .collect(ImmutableSet.toImmutableSet());
  }

  @Test
  void testGetNextHopInterfacesByRoute() throws IOException {
    String iface1 = "iface1";
    String iface2 = "iface2";
    String iface3 = "iface3";
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.1");
    _ib.setName(iface1).setAddress(ConcreteInterfaceAddress.create(ip1, 24)).build();
    _ib.setName(iface2).setAddress(ConcreteInterfaceAddress.create(ip2, 24)).build();
    _ib.setName(iface3).setAddress(ConcreteInterfaceAddress.create(ip2, 24)).build();

    Batfish batfish =
        BatfishTestUtils.getBatfish(ImmutableSortedMap.of(_config.getHostname(), _config), folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    Fib fib =
        batfish
            .loadDataPlane(batfish.getSnapshot())
            .getFibs()
            .get(_config.getHostname())
            .get(Configuration.DEFAULT_VRF_NAME);

    // Should have one LocalRoute per interface (also one ConnectedRoute, but LocalRoute will have
    // longer prefix match). Should see only iface1 in interfaces to ip1.
    Set<FibEntry> nextHopsToIp1 = fib.get(ip1);
    assertThat(
        nextHopsToIp1, contains(hasAction(isFibForwardActionThat(hasInterfaceName(iface1)))));

    // Should see interfaces iface2 and iface3 in interfaces to ip2.
    Set<FibEntry> nextHopsIp2 = fib.get(ip2);
    assertThat(
        nextHopsIp2,
        containsInAnyOrder(
            ImmutableList.of(
                hasAction(isFibForwardActionThat(hasInterfaceName(iface2))),
                hasAction(isFibForwardActionThat(hasInterfaceName(iface3))))));
  }

  @Test
  void testNextHopInterfaceTakesPrecedence() throws IOException {
    _ib.setName(FAST_ETHERNET_0).setAddresses(NODE1_PHYSICAL_NETWORK).build();
    /*
     * Both next hop IP and interface on the static route. Interface should take precedence
     * EXTERNAL_IP should be ignored (i.e., not resolved recursively) -- there will not be a route
     * for it.
     */
    _vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(DST_IP.toPrefix())
                .setNextHopInterface(FAST_ETHERNET_0)
                .setNextHopIp(EXTERNAL_IP)
                .setAdministrativeCost(1)
                .build()));

    Batfish batfish =
        BatfishTestUtils.getBatfish(ImmutableSortedMap.of(_config.getHostname(), _config), folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    Fib fib =
        batfish
            .loadDataPlane(batfish.getSnapshot())
            .getFibs()
            .get(_config.getHostname())
            .get(Configuration.DEFAULT_VRF_NAME);

    assertThat(
        fib.get(DST_IP),
        contains(hasAction(isFibForwardActionThat(hasInterfaceName(FAST_ETHERNET_0)))));
  }

  @Test
  void testNextHopIpIsResolved() throws IOException {
    _ib.setName(FAST_ETHERNET_0).setAddresses(NODE1_PHYSICAL_NETWORK).build();
    /*
     * Only next hop ip on the static route.
     * Next hop IP should be resolved, and match the connected route on FastEthernet0/0
     */
    _vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(DST_IP.toPrefix())
                .setNextHopIp(Ip.parse("2.1.1.1"))
                .setAdministrativeCost(1)
                .build()));

    Batfish batfish =
        BatfishTestUtils.getBatfish(ImmutableSortedMap.of(_config.getHostname(), _config), folder);
    batfish.computeDataPlane(batfish.getSnapshot());
    Fib fib =
        batfish
            .loadDataPlane(batfish.getSnapshot())
            .getFibs()
            .get(_config.getHostname())
            .get(Configuration.DEFAULT_VRF_NAME);

    assertThat(
        fib.get(DST_IP),
        contains(hasAction(isFibForwardActionThat(hasInterfaceName(FAST_ETHERNET_0)))));
  }

  @Test
  void testNonForwardingRoutesNotInFib() {
    Rib rib = new Rib();

    StaticRoute nonForwardingInterfaceRoute =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopInterface("Eth1")
            .setAdministrativeCost(1)
            .setNonForwarding(true)
            .build();
    StaticRoute nonForwardingNextHopIpRoute =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("3.3.3.0/24"))
            .setNextHopIp(Ip.parse("2.2.2.1"))
            .setAdministrativeCost(1)
            .setNonForwarding(true)
            .setRecursive(true)
            .build();
    StaticRoute forwardingRoute =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("2.2.2.0/24"))
            .setNextHopInterface("Eth1")
            .setAdministrativeCost(1)
            .setNonForwarding(false)
            .build();

    rib.mergeRoute(annotateRoute(nonForwardingNextHopIpRoute));
    rib.mergeRoute(annotateRoute(nonForwardingInterfaceRoute));
    rib.mergeRoute(annotateRoute(forwardingRoute));

    Fib fib = new FibImpl(rib, ResolutionRestriction.alwaysTrue());
    Set<AbstractRoute> fibRoutes = getTopLevelRoutesByInterface(fib, "Eth1");

    assertThat(fibRoutes, not(hasItem(hasPrefix(Prefix.parse("1.1.1.0/24")))));
    assertThat(fibRoutes, not(hasItem(hasPrefix(Prefix.parse("3.3.3.0/24")))));
    assertThat(fibRoutes, hasItem(hasPrefix(Prefix.parse("2.2.2.0/24"))));
  }

  @Test
  void testNextVrfRouteInFib() {
    Rib rib = new Rib();
    String nextVrf = "nextVrf";

    StaticRoute nextVrfRoute =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.ZERO)
            .setNextHop(org.batfish.datamodel.route.nh.NextHopVrf.of("nextVrf"))
            .setAdministrativeCost(1)
            .build();

    rib.mergeRoute(annotateRoute(nextVrfRoute));

    Fib fib = new FibImpl(rib, ResolutionRestriction.alwaysTrue());

    assertThat(
        fib.allEntries(),
        contains(new FibEntry(FibNextVrf.of(nextVrf), ImmutableList.of(nextVrfRoute))));
  }

  /**
   * Routes sharing a next hop IP share its resolution, except where a route's own network is on the
   * resolution path: that route is declared a loop, while another route through the same next hop
   * is not.
   */
  @Test
  void testResolutionSharedAcrossRoutesWithSameNextHopExceptLoops() {
    Rib rib = new Rib();
    ConnectedRoute connected = new ConnectedRoute(Prefix.parse("10.0.0.0/24"), "Eth1");
    // Two ECMP routes for 2.2.2.0/24: one resolves via the connected route, the other via 2.2.2.2,
    // which is inside its own network.
    StaticRoute viaConnected =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("2.2.2.0/24"))
            .setNextHopIp(Ip.parse("10.0.0.1"))
            .setAdministrativeCost(1)
            .build();
    StaticRoute selfLoop =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("2.2.2.0/24"))
            .setNextHopIp(Ip.parse("2.2.2.2"))
            .setAdministrativeCost(1)
            .build();
    // Same next hop as selfLoop, but not on its own path.
    StaticRoute other =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopIp(Ip.parse("2.2.2.2"))
            .setAdministrativeCost(1)
            .build();
    StaticRoute another =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("3.3.3.0/24"))
            .setNextHopIp(Ip.parse("2.2.2.2"))
            .setAdministrativeCost(1)
            .build();
    rib.mergeRoute(annotateRoute(connected));
    rib.mergeRoute(annotateRoute(viaConnected));
    rib.mergeRoute(annotateRoute(selfLoop));
    rib.mergeRoute(annotateRoute(other));
    rib.mergeRoute(annotateRoute(another));

    Fib fib = new FibImpl(rib, alwaysTrue());

    // selfLoop only resolves to a loop: the LPM for its next hop is its own network.
    assertThat(
        fib.get(Ip.parse("2.2.2.5")),
        containsInAnyOrder(
            hasAction(isFibForwardActionThat(hasInterfaceName("Eth1"))),
            hasAction(equalTo(FibNullRoute.INSTANCE))));
    // other and another resolve to Eth1 via viaConnected, and to a loop via selfLoop.
    for (Ip ip : ImmutableList.of(Ip.parse("1.1.1.1"), Ip.parse("3.3.3.3"))) {
      assertThat(
          fib.get(ip),
          containsInAnyOrder(
              hasAction(isFibForwardActionThat(hasInterfaceName("Eth1"))),
              hasAction(equalTo(FibNullRoute.INSTANCE))));
    }
  }

  @Test
  void testResolutionWhenNextHopMatchesNonForwardingRoute() {
    Rib rib = new Rib();

    StaticRoute nonForwardingRoute =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setNextHopInterface("Eth2")
            .setAdministrativeCost(1)
            .setNonForwarding(true)
            .build();

    StaticRoute forwardingLessSpecificRoute =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/31"))
            .setNextHopInterface("Eth1")
            .setAdministrativeCost(1)
            .setNonForwarding(false)
            .build();

    StaticRoute testRoute =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("2.2.2.0/24"))
            .setNextHopIp(Ip.parse("1.1.1.1")) // matches both routes defined above
            .setAdministrativeCost(1)
            .setNonForwarding(false)
            .build();

    rib.mergeRoute(annotateRoute(nonForwardingRoute));
    rib.mergeRoute(annotateRoute(forwardingLessSpecificRoute));
    rib.mergeRoute(annotateRoute(testRoute));

    Fib fib = new FibImpl(rib, alwaysTrue());
    Set<AbstractRoute> fibRoutesEth1 = getTopLevelRoutesByInterface(fib, "Eth1");

    /* 2.2.2.0/24 should resolve to the "forwardingLessSpecificRoute" and thus eth1 */
    assertThat(fibRoutesEth1, hasItem(hasPrefix(Prefix.parse("2.2.2.0/24"))));

    /* Nothing can resolve to "eth2" */
    Set<AbstractRoute> fibRoutesEth2 = getTopLevelRoutesByInterface(fib, "Eth2");
    assertThat(fibRoutesEth2, empty());
  }

  @Test
  void testResolutionWhenNextHopMatchesRestrictionViolatingRoute() {
    Rib rib = new Rib();

    StaticRoute restrictionViolatingRoute =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setNextHopInterface("Eth2")
            .setAdministrativeCost(1)
            .build();

    StaticRoute forwardingLessSpecificRoute =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/31"))
            .setNextHopInterface("Eth1")
            .setAdministrativeCost(1)
            .build();

    StaticRoute testRoute =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("2.2.2.0/24"))
            .setNextHopIp(Ip.parse("1.1.1.1")) // matches both routes defined above
            .setAdministrativeCost(1)
            .build();

    rib.mergeRoute(annotateRoute(restrictionViolatingRoute));
    rib.mergeRoute(annotateRoute(forwardingLessSpecificRoute));
    rib.mergeRoute(annotateRoute(testRoute));

    Fib fib = new FibImpl(rib, r -> !r.getAbstractRoute().getNextHopInterface().equals("Eth2"));
    Set<AbstractRoute> fibRoutesEth1 = getTopLevelRoutesByInterface(fib, "Eth1");

    /* 2.2.2.0/24 should resolve to the "forwardingLessSpecificRoute" and thus eth1 */
    assertThat(fibRoutesEth1, hasItem(hasPrefix(Prefix.parse("2.2.2.0/24"))));

    // Only the route violating the restriction can resolve to eth2; nothing should recursively
    // resolve to Eth2.
    Set<AbstractRoute> fibRoutesEth2 = getTopLevelRoutesByInterface(fib, "Eth2");
    assertThat(fibRoutesEth2, contains(restrictionViolatingRoute));
  }

  @Test
  void testResolutionWhenRecursiveStaticRouteNextHopMatchesRestrictionViolatingConnectedRoute() {
    Rib rib = new Rib();

    ConnectedRoute restrictionViolatingRoute =
        new ConnectedRoute(Prefix.strict("1.1.1.1/32"), "Eth2");

    StaticRoute forwardingLessSpecificRoute =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/31"))
            .setNextHopInterface("Eth1")
            .setAdministrativeCost(1)
            .build();

    StaticRoute testRoute =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("2.2.2.0/24"))
            .setNextHopIp(Ip.parse("1.1.1.1")) // matches both routes defined above
            .setAdministrativeCost(1)
            .setRecursive(true)
            .build();

    rib.mergeRoute(annotateRoute(restrictionViolatingRoute));
    rib.mergeRoute(annotateRoute(forwardingLessSpecificRoute));
    rib.mergeRoute(annotateRoute(testRoute));

    Fib fib = new FibImpl(rib, r -> !r.getAbstractRoute().getNextHopInterface().equals("Eth2"));

    // The route violating the restriction uses eth2; the recursive static route also resolves to
    // eth2 despite the route for 1.1.1.1/32 violating restriction because the former route is a
    // recursive static route and the latter route is a connected route.
    Set<AbstractRoute> fibRoutesEth2 = getTopLevelRoutesByInterface(fib, "Eth2");
    assertThat(fibRoutesEth2, containsInAnyOrder(testRoute, restrictionViolatingRoute));
  }

  @Test
  void testResolutionWhenNextHopMatchesNonForwardingRouteWithECMP() {
    Rib rib = new Rib();

    StaticRoute nonForwardingRoute =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setNextHopInterface("Eth2")
            .setAdministrativeCost(1)
            .setNonForwarding(true)
            .build();

    StaticRoute ecmpForwardingRoute1 =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setNextHopInterface("Eth3")
            .setAdministrativeCost(1)
            .setNonForwarding(false)
            .build();
    StaticRoute ecmpForwardingRoute2 =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setNextHopInterface("Eth4")
            .setAdministrativeCost(1)
            .setNonForwarding(false)
            .build();

    StaticRoute forwardingLessSpecificRoute =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/31"))
            .setNextHopInterface("Eth1")
            .setAdministrativeCost(1)
            .setNonForwarding(false)
            .build();

    final Prefix TEST_PREFIX = Prefix.parse("2.2.2.0/24");
    StaticRoute testRoute =
        StaticRoute.testBuilder()
            .setNetwork(TEST_PREFIX)
            .setNextHopIp(Ip.parse("1.1.1.1")) // matches multiple routes defined above
            .setAdministrativeCost(1)
            .setNonForwarding(false)
            .build();

    rib.mergeRoute(annotateRoute(nonForwardingRoute));
    rib.mergeRoute(annotateRoute(forwardingLessSpecificRoute));
    rib.mergeRoute(annotateRoute(testRoute));
    rib.mergeRoute(annotateRoute(ecmpForwardingRoute1));
    rib.mergeRoute(annotateRoute(ecmpForwardingRoute2));

    Fib fib = new FibImpl(rib, alwaysTrue());

    /* 2.2.2.0/24 should resolve to eth3 and eth4*/
    assertThat(getTopLevelRoutesByInterface(fib, "Eth3"), hasItem(hasPrefix(TEST_PREFIX)));
    assertThat(getTopLevelRoutesByInterface(fib, "Eth4"), hasItem(hasPrefix(TEST_PREFIX)));

    /* 2.2.2.0/24 should NOT resolve to "forwardingLessSpecificRoute" (and thus Eth1)
     * because more specific route exists to eth3/4
     */
    assertThat(getTopLevelRoutesByInterface(fib, "Eth1"), not(hasItem(hasPrefix(TEST_PREFIX))));

    /* Nothing can resolve to eth2 */
    Set<AbstractRoute> fibRoutesEth2 = getTopLevelRoutesByInterface(fib, "Eth2");
    assertThat(fibRoutesEth2, empty());
  }

  @Test
  void testResolutionWhenNextHopRouteDoesNotMatchRestrictionWithECMP() {
    Rib rib = new Rib();

    StaticRoute restrictionViolatingRoute =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setNextHopInterface("Eth2")
            .setAdministrativeCost(1)
            .build();

    StaticRoute ecmpForwardingRoute1 =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setNextHopInterface("Eth3")
            .setAdministrativeCost(1)
            .build();
    StaticRoute ecmpForwardingRoute2 =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setNextHopInterface("Eth4")
            .setAdministrativeCost(1)
            .build();

    StaticRoute forwardingLessSpecificRoute =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/31"))
            .setNextHopInterface("Eth1")
            .setAdministrativeCost(1)
            .build();

    final Prefix TEST_PREFIX = Prefix.parse("2.2.2.0/24");
    StaticRoute testRoute =
        StaticRoute.testBuilder()
            .setNetwork(TEST_PREFIX)
            .setNextHopIp(Ip.parse("1.1.1.1")) // matches multiple routes defined above
            .setAdministrativeCost(1)
            .build();

    rib.mergeRoute(annotateRoute(restrictionViolatingRoute));
    rib.mergeRoute(annotateRoute(forwardingLessSpecificRoute));
    rib.mergeRoute(annotateRoute(testRoute));
    rib.mergeRoute(annotateRoute(ecmpForwardingRoute1));
    rib.mergeRoute(annotateRoute(ecmpForwardingRoute2));

    Fib fib = new FibImpl(rib, r -> !r.getAbstractRoute().getNextHopInterface().equals("Eth2"));

    /* 2.2.2.0/24 should resolve to eth3 and eth4*/
    assertThat(getTopLevelRoutesByInterface(fib, "Eth3"), hasItem(hasPrefix(TEST_PREFIX)));
    assertThat(getTopLevelRoutesByInterface(fib, "Eth4"), hasItem(hasPrefix(TEST_PREFIX)));

    /* 2.2.2.0/24 should NOT resolve to "forwardingLessSpecificRoute" (and thus Eth1)
     * because more specific route exists to eth3/4
     */
    assertThat(getTopLevelRoutesByInterface(fib, "Eth1"), not(hasItem(hasPrefix(TEST_PREFIX))));

    // Only the route violating the restriction can resolve to eth2; nothing should recursively
    // resolve to Eth2.
    Set<AbstractRoute> fibRoutesEth2 = getTopLevelRoutesByInterface(fib, "Eth2");
    assertThat(fibRoutesEth2, contains(restrictionViolatingRoute));
  }

  @Test
  void testFibExportFilter() {
    Rib rib = new Rib();

    StaticRoute allowedRoute =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopInterface("Eth1")
            .setAdministrativeCost(1)
            .build();
    StaticRoute deniedRoute =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("2.2.2.0/24"))
            .setNextHopInterface("Eth1")
            .setAdministrativeCost(1)
            .build();

    rib.mergeRoute(annotateRoute(allowedRoute));
    rib.mergeRoute(annotateRoute(deniedRoute));

    // FIB export filter that only permits 1.1.1.0/24
    Fib fib =
        new FibImpl(rib, alwaysTrue(), r -> r.getNetwork().equals(Prefix.parse("1.1.1.0/24")));

    Set<AbstractRoute> fibRoutes = getTopLevelRoutesByInterface(fib, "Eth1");
    assertThat(fibRoutes, hasItem(hasPrefix(Prefix.parse("1.1.1.0/24"))));
    assertThat(fibRoutes, not(hasItem(hasPrefix(Prefix.parse("2.2.2.0/24")))));
  }

  private static StaticRoute interfaceRoute(Prefix network, boolean nonForwarding) {
    return StaticRoute.testBuilder()
        .setNetwork(network)
        .setNextHopInterface("Eth1")
        .setAdministrativeCost(1)
        .setNonForwarding(nonForwarding)
        .build();
  }

  @Test
  void testMatchingIpsExcludesNextLongerForwardingPrefixes() {
    Prefix p8 = Prefix.parse("10.0.0.0/8");
    Prefix p16a = Prefix.parse("10.1.0.0/16");
    Prefix p24 = Prefix.parse("10.1.1.0/24");
    Prefix p16b = Prefix.parse("10.2.0.0/16");
    Prefix nonForwarding = Prefix.parse("10.3.0.0/16");
    Rib rib = new Rib();
    for (Prefix p : ImmutableList.of(p8, p16a, p24, p16b)) {
      rib.mergeRoute(annotateRoute(interfaceRoute(p, false)));
    }
    rib.mergeRoute(annotateRoute(interfaceRoute(nonForwarding, true)));
    Fib fib = new FibImpl(rib, alwaysTrue());

    // Only the next-longer forwarding prefixes are excluded: not 10.1.1.0/24, which 10.1.0.0/16
    // already excludes, and not the non-forwarding 10.3.0.0/16.
    assertThat(
        fib.matchingIps(p8),
        equalTo(
            IpWildcardSetIpSpace.create(
                ImmutableSet.of(IpWildcard.create(p16a), IpWildcard.create(p16b)),
                ImmutableSet.of(IpWildcard.create(p8)))));
    assertThat(
        fib.matchingIps(p16a),
        equalTo(
            IpWildcardSetIpSpace.create(
                ImmutableSet.of(IpWildcard.create(p24)),
                ImmutableSet.of(IpWildcard.create(p16a)))));
    assertThat(fib.matchingIps(p24), equalTo(p24.toIpSpace()));
    assertThat(fib.matchingIps(p16b), equalTo(p16b.toIpSpace()));
  }

  /** Random prefixes clustered enough that they nest, collide, and share subtrees. */
  private static Prefix randomPrefix(Random rng) {
    int len = rng.nextInt(33);
    long ip =
        rng.nextBoolean()
            ? 0x0A000000L | rng.nextInt(1 << 16)
            : 0x0A000000L | (rng.nextInt(8) << 8) | rng.nextInt(4);
    return Prefix.create(Ip.create(ip), len);
  }

  /** An IP is in {@code matchingIps(p)} iff {@code p} is its longest forwarding prefix match. */
  @Test
  void testMatchingIpsIsLongestPrefixMatch() {
    Random rng = new Random(20261120);
    for (int trial = 0; trial < 40; trial++) {
      Rib rib = new Rib();
      List<Prefix> forwarding = new ArrayList<>();
      if (trial % 2 == 0) {
        rib.mergeRoute(annotateRoute(interfaceRoute(Prefix.ZERO, false)));
        forwarding.add(Prefix.ZERO);
      }
      int routes = 1 + rng.nextInt(60);
      for (int i = 0; i < routes; i++) {
        Prefix p = randomPrefix(rng);
        boolean nonForwarding = rng.nextInt(5) == 0;
        rib.mergeRoute(annotateRoute(interfaceRoute(p, nonForwarding)));
        if (!nonForwarding) {
          forwarding.add(p);
        }
      }
      Fib fib = new FibImpl(rib, alwaysTrue());
      Set<Prefix> fibPrefixes =
          fib.allEntries().stream()
              .map(e -> e.getTopLevelRoute().getNetwork())
              .collect(ImmutableSet.toImmutableSet());
      assertThat(fibPrefixes, equalTo(ImmutableSet.copyOf(forwarding)));

      for (int i = 0; i < 200; i++) {
        Ip ip = randomPrefix(rng).getStartIp();
        if (rng.nextBoolean()) {
          ip = Ip.create(ip.asLong() | rng.nextInt(256));
        }
        Ip finalIp = ip;
        Prefix longest =
            fibPrefixes.stream()
                .filter(p -> p.containsIp(finalIp))
                .max(Comparator.comparingInt(Prefix::getPrefixLength))
                .orElse(null);
        for (Prefix p : fibPrefixes) {
          assertThat(
              "trial " + trial + " " + ip + " in matchingIps(" + p + ")",
              fib.matchingIps(p).containsIp(ip, ImmutableMap.of()),
              equalTo(p.equals(longest)));
        }
      }
    }
  }

  private static AnnotatedRoute<AbstractRoute> ecmpRoute(Prefix network, String iface) {
    return annotateRoute(
        StaticRoute.testBuilder()
            .setNetwork(network)
            .setNextHopInterface(iface)
            .setAdministrativeCost(1)
            .build());
  }

  @Test
  void testEcmpRoutesAllInstalled() {
    Prefix network = Prefix.parse("10.0.0.0/24");
    Rib rib = new Rib();
    for (int i = 0; i < 8; i++) {
      rib.mergeRoute(ecmpRoute(network, "Eth" + i));
    }
    rib.mergeRoute(ecmpRoute(Prefix.parse("10.0.1.0/24"), "Eth0"));
    Fib fib = new FibImpl(rib, alwaysTrue());

    Set<FibEntry> entries = fib.get(Ip.parse("10.0.0.1"));
    assertThat(entries, hasSize(8));
    assertThat(
        entries.stream()
            .map(e -> ((FibForward) e.getAction()).getInterfaceName())
            .collect(ImmutableSet.toImmutableSet()),
        equalTo(
            IntStream.range(0, 8).mapToObj(i -> "Eth" + i).collect(ImmutableSet.toImmutableSet())));
    assertThat(fib.allEntries(), hasSize(9));
  }

  /** A RIB need not return a network's routes together; they are still all installed. */
  @Test
  void testRoutesForOneNetworkNotTogetherAllInstalled() {
    Prefix p1 = Prefix.parse("10.0.0.0/24");
    Prefix p2 = Prefix.parse("10.0.1.0/24");
    MockRib rib =
        MockRib.builder()
            .setRoutes(
                ImmutableSet.of(
                    ecmpRoute(p1, "Eth1"),
                    ecmpRoute(p2, "Eth2"),
                    ecmpRoute(p1, "Eth3"),
                    ecmpRoute(p1, "Eth4")))
            .build();
    Fib fib = new FibImpl(rib, alwaysTrue());

    assertThat(
        fib.get(Ip.parse("10.0.0.1")).stream()
            .map(e -> ((FibForward) e.getAction()).getInterfaceName())
            .collect(ImmutableSet.toImmutableSet()),
        equalTo(ImmutableSet.of("Eth1", "Eth3", "Eth4")));
    assertThat(fib.get(Ip.parse("10.0.1.1")), hasSize(1));
  }
}
