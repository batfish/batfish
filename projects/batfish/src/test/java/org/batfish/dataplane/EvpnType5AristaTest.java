package org.batfish.dataplane;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.OriginMechanism.REDISTRIBUTE;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.AnnotatedRouteMatchers.hasRoute;
import static org.batfish.vendor.arista.representation.AristaConfiguration.DEFAULT_EBGP_ADMIN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.EvpnRoute;
import org.batfish.datamodel.EvpnType5Route;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ReceivedFromSelf;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.dataplane.ibdp.IncrementalDataPlane;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.vendor.arista.representation.AristaConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class EvpnType5AristaTest {
  private static final String TESTCONFIGS_PATH = "org/batfish/dataplane/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testType5RoutePresence() throws IOException {
    /*
    Setup: Single node containing three VRFs.
    - Default VRF does not redistribute anything, but has an EVPN address family configured
    - Vrf vrf1 redistributes connected and exports EVPN with route-target 15004:15004
    - Vrf vrf2 imports EVPN with route-target 15004:15004
    Should see the local BGPv4 route in vrf1 get exported into default VRF as an EVPN route, and then
    imported from the default VRF into vrf2 as a BGPv4 route.
     */
    String hostname = "evpn-type5-arista";
    Batfish batfish =
        BatfishTestUtils.getBatfishForTextConfigs(_folder, TESTCONFIGS_PATH + hostname);
    batfish.computeDataPlane(batfish.getSnapshot()); // compute and cache the dataPlane
    IncrementalDataPlane dp = (IncrementalDataPlane) batfish.loadDataPlane(batfish.getSnapshot());
    String vrf1 = "vrf1";
    String vrf2 = "vrf2";
    Ip originatorIp = Ip.parse("12.12.12.2"); // IP of BGP route originator
    Prefix prefix = Prefix.parse("12.12.12.0/24"); // prefix of the connected route in vrf1

    // The connected route that vrf1 redistributes into BGP should be a connected route in vrf1, an
    // EVPN route in the default VRF (so no appearance in main RIB), and a BGPv4 route in vrf2.
    SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>> ribs =
        dp.getRibsForTesting().get(hostname);
    assertThat(ribs.get(DEFAULT_VRF_NAME).getRoutes(prefix), empty());
    assertThat(
        ribs.get(vrf1).getRoutes(prefix), contains(hasRoute(instanceOf(ConnectedRoute.class))));
    assertThat(ribs.get(vrf2).getRoutes(prefix), contains(hasRoute(instanceOf(Bgpv4Route.class))));

    // Route-map sets attributes of vrf1's local BGP route. They should be copied to the EVPN route
    Community rmCommunity = StandardCommunity.of(12345);
    long rmAs = 54321;

    // vrf1 should have a local BGPv4 route due to the network statement. The default VRF should
    // have no BGPv4 routes (exporting EVPN from vrf1 should impact default VRF's EVPN RIB only).
    Map<String, Set<Bgpv4Route>> bgpRoutes = dp.getBgpRoutes().row(hostname);
    assertThat(bgpRoutes.get(DEFAULT_VRF_NAME), empty());
    Bgpv4Route vrf1BgpRoute = Iterables.getOnlyElement(bgpRoutes.get(vrf1));
    assertThat(vrf1BgpRoute, hasPrefix(prefix));
    assertThat(vrf1BgpRoute.getCommunities(), equalTo(CommunitySet.of(rmCommunity)));
    assertThat(vrf1BgpRoute.getAsPath(), equalTo(AsPath.ofSingletonAsSets(rmAs)));
    // vrf2 should have a BGPv4 route because it imports the EVPN route target from the default VRF.
    Bgpv4Route vrf2BgpRoute = Iterables.getOnlyElement(bgpRoutes.get(vrf2));
    Bgpv4Route expectedVrf2BgpRoute =
        Bgpv4Route.builder()
            .setNetwork(prefix)
            .setCommunities(ImmutableSet.of(rmCommunity, ExtendedCommunity.target(15004, 15004)))
            .setAdmin(DEFAULT_EBGP_ADMIN)
            .setAsPath(AsPath.ofSingletonAsSets(rmAs))
            // REDISTRIBUTE rather than NETWORK because Arista has a combined network statement +
            // redistribution policy (a route can't be originated via both mechanisms)
            .setOriginMechanism(REDISTRIBUTE)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHop(NextHopDiscard.instance())
            .setSrcProtocol(RoutingProtocol.CONNECTED)
            .setReceivedFrom(ReceivedFromSelf.instance())
            .setOriginatorIp(originatorIp)
            .setWeight(AristaConfiguration.DEFAULT_LOCAL_BGP_WEIGHT)
            .build();
    assertThat(vrf2BgpRoute, equalTo(expectedVrf2BgpRoute));

    // Only default VRF should have an EVPN route.
    Map<String, Set<EvpnRoute<?, ?>>> evpnRoutes = dp.getEvpnRoutes().row(hostname);
    assertThat(evpnRoutes.get(vrf1), empty());
    assertThat(evpnRoutes.get(vrf2), empty());
    EvpnRoute<?, ?> exportedEvpnRoute = Iterables.getOnlyElement(evpnRoutes.get(DEFAULT_VRF_NAME));
    EvpnType5Route expectedEvpnRoute =
        EvpnType5Route.builder()
            .setNetwork(prefix)
            .setRouteDistinguisher(RouteDistinguisher.from(Ip.parse("192.168.255.1"), 15004))
            .setCommunities(ImmutableSet.of(rmCommunity, ExtendedCommunity.target(15004, 15004)))
            .setAsPath(AsPath.ofSingletonAsSets(rmAs))
            // REDISTRIBUTE rather than NETWORK because Arista has a combined network statement +
            // redistribution policy (a route can't be originated via both mechanisms)
            .setOriginMechanism(REDISTRIBUTE)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHop(NextHopDiscard.instance())
            .setSrcProtocol(RoutingProtocol.CONNECTED)
            .setReceivedFrom(ReceivedFromSelf.instance())
            .setOriginatorIp(originatorIp)
            .setVni(15004)
            .setWeight(AristaConfiguration.DEFAULT_LOCAL_BGP_WEIGHT)
            .build();
    assertThat(exportedEvpnRoute, equalTo(expectedEvpnRoute));
  }
}
