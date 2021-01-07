package org.batfish.dataplane;

import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.FibActionMatchers.hasInterfaceName;
import static org.batfish.datamodel.matchers.FibActionMatchers.isFibForwardActionThat;
import static org.batfish.datamodel.matchers.FibEntryMatchers.hasAction;
import static org.batfish.dataplane.ibdp.TestUtils.annotateRoute;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.Set;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FibEntry;
import org.batfish.datamodel.FibForward;
import org.batfish.datamodel.FibImpl;
import org.batfish.datamodel.FibNextVrf;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.dataplane.rib.Rib;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link FibImpl} */
@RunWith(JUnit4.class)
public class FibImplTest {
  @Rule public TemporaryFolder folder = new TemporaryFolder();

  private static final Ip DST_IP = Ip.parse("3.3.3.3");
  private static final String NODE1 = "node1";
  private static final String FAST_ETHERNET_0 = "FastEthernet0/0";
  private static final ConcreteInterfaceAddress NODE1_PHYSICAL_NETWORK =
      ConcreteInterfaceAddress.parse("2.0.0.1/8");
  private static final Ip EXTERNAL_IP = Ip.parse("7.7.7.7");

  private Interface.Builder _ib;
  private Configuration _config;
  private Vrf _vrf;

  @Before
  public void setup() {
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
  public void testGetNextHopInterfacesByRoute() throws IOException {
    String iface1 = "iface1";
    String iface2 = "iface2";
    String iface3 = "iface3";
    Ip ip1 = Ip.parse("1.1.1.0");
    Ip ip2 = Ip.parse("2.2.2.0");
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
  public void testNextHopInterfaceTakesPrecedence() throws IOException {
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
  public void testNextHopIpIsResolved() throws IOException {
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
  public void testNonForwardingRouteNotInFib() {
    Rib rib = new Rib();

    StaticRoute nonForwardingRoute =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopInterface("Eth1")
            .setAdministrativeCost(1)
            .setNonForwarding(true)
            .build();
    StaticRoute forwardingRoute =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("2.2.2.0/24"))
            .setNextHopInterface("Eth1")
            .setAdministrativeCost(1)
            .setNonForwarding(false)
            .build();

    rib.mergeRoute(annotateRoute(nonForwardingRoute));
    rib.mergeRoute(annotateRoute(forwardingRoute));

    Fib fib = new FibImpl(rib);
    Set<AbstractRoute> fibRoutes = getTopLevelRoutesByInterface(fib, "Eth1");

    assertThat(fibRoutes, not(hasItem(hasPrefix(Prefix.parse("1.1.1.0/24")))));
    assertThat(fibRoutes, hasItem(hasPrefix(Prefix.parse("2.2.2.0/24"))));
  }

  @Test
  public void testNextVrfRouteInFib() {
    Rib rib = new Rib();
    String nextVrf = "nextVrf";

    StaticRoute nextVrfRoute =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.ZERO)
            .setNextHop(org.batfish.datamodel.route.nh.NextHopVrf.of("nextVrf"))
            .setAdministrativeCost(1)
            .build();

    rib.mergeRoute(annotateRoute(nextVrfRoute));

    Fib fib = new FibImpl(rib);

    assertThat(
        fib.allEntries(),
        contains(new FibEntry(new FibNextVrf(nextVrf), ImmutableList.of(nextVrfRoute))));
  }

  @Test
  public void testResolutionWhenNextHopMatchesNonForwardingRoute() {
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

    Fib fib = new FibImpl(rib);
    Set<AbstractRoute> fibRoutesEth1 = getTopLevelRoutesByInterface(fib, "Eth1");

    /* 2.2.2.0/24 should resolve to the "forwardingLessSpecificRoute" and thus eth1 */
    assertThat(fibRoutesEth1, hasItem(hasPrefix(Prefix.parse("2.2.2.0/24"))));

    /* Nothing can resolve to "eth2" */
    Set<AbstractRoute> fibRoutesEth2 = getTopLevelRoutesByInterface(fib, "Eth2");
    assertThat(fibRoutesEth2, empty());
  }

  @Test
  public void testResolutionWhenNextHopMatchesNonForwardingRouteWithECMP() {
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

    Fib fib = new FibImpl(rib);

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
}
