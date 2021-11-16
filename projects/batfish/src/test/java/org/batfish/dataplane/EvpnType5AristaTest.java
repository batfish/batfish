package org.batfish.dataplane;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.instanceOf;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.EvpnRoute;
import org.batfish.datamodel.EvpnType5Route;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class EvpnType5AristaTest {
  private static final String TESTCONFIGS_PATH = "org/batfish/dataplane/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testType5RoutePresence() throws IOException {
    /*
    Setup: Single node containing two VRFs.
    - Default VRF does not redistribute anything, but has an EVPN address family configured
    - Vrf vrf1 redistributes connected and exports EVPN with route-target 15004:15004
    Should see the local BGPv4 route in vrf1 get exported into default VRF as an EVPN route.
     */
    String hostname = "evpn-type5-arista";
    Batfish batfish =
        BatfishTestUtils.getBatfishForTextConfigs(_folder, TESTCONFIGS_PATH + hostname);
    batfish.computeDataPlane(batfish.getSnapshot()); // compute and cache the dataPlane
    DataPlane dp = batfish.loadDataPlane(batfish.getSnapshot());
    String vrf1 = "vrf1";
    Prefix prefix = Prefix.parse("12.12.12.0/24"); // prefix of the connected route in vrf1

    // Neither VRF should have any BGP or EVPN routes in the main RIB.
    SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>> ribs = dp.getRibs().get(hostname);
    ribs.values()
        .forEach(rib -> assertThat(rib.getRoutes(), everyItem(instanceOf(ConnectedRoute.class))));

    // Only vrf1 should have BGP routes; exporting EVPN should not affect default VRF's BGP RIB.
    Map<String, Set<Bgpv4Route>> bgpRoutes = dp.getBgpRoutes().row(hostname);
    assertThat(bgpRoutes.get(DEFAULT_VRF_NAME), empty());
    assertThat(bgpRoutes.get(vrf1), contains(hasPrefix(prefix)));

    // Only default VRF should have an EVPN route.
    Map<String, Set<EvpnRoute<?, ?>>> evpnRoutes = dp.getEvpnRoutes().row(hostname);
    assertThat(evpnRoutes.get(vrf1), empty());
    EvpnType5Route expectedEvpnRoute =
        EvpnType5Route.builder()
            .setNetwork(prefix)
            .setRouteDistinguisher(RouteDistinguisher.from(Ip.parse("192.168.255.1"), 15004))
            .setNonRouting(true)
            .setCommunities(ImmutableSet.of(ExtendedCommunity.target(15004, 15004)))
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHop(NextHopDiscard.instance())
            // TODO Is it correct to copy these attributes from the source BGP route?
            .setSrcProtocol(RoutingProtocol.CONNECTED)
            .setReceivedFromIp(Ip.ZERO)
            .setOriginatorIp(Ip.AUTO) // TODO Is this valid, even on the original BGP route?
            .setAdmin(200)
            .build();
    assertThat(evpnRoutes.get(DEFAULT_VRF_NAME), contains(expectedEvpnRoute));
  }
}
