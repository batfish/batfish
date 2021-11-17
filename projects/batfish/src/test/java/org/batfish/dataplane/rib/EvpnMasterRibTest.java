package org.batfish.dataplane.rib;

import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.NO_PREFERENCE;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.EvpnType5Route;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.junit.Test;

/** Test of {@link EvpnMasterRib}. */
public final class EvpnMasterRibTest {

  @Test
  public void testRdIndependence() {
    EvpnMasterRib<EvpnType5Route> rib =
        new EvpnMasterRib<>(BgpTieBreaker.ROUTER_ID, null, false, NO_PREFERENCE);
    EvpnType5Route.Builder rb =
        EvpnType5Route.builder()
            .setNetwork(Prefix.ZERO)
            .setNextHop(NextHopIp.of(Ip.parse("10.0.0.1")))
            .setProtocol(RoutingProtocol.BGP)
            .setOriginType(OriginType.IGP)
            .setOriginatorIp(Ip.ZERO);
    EvpnType5Route route1 =
        rb.setRouteDistinguisher(RouteDistinguisher.from(1, 1L)).setLocalPreference(10).build();
    EvpnType5Route route21 =
        rb.setRouteDistinguisher(RouteDistinguisher.from(2, 2L)).setLocalPreference(20).build();
    EvpnType5Route route22 =
        rb.setRouteDistinguisher(RouteDistinguisher.from(2, 2L)).setLocalPreference(15).build();

    // route1 and route21 should be best. route22 should be backup
    assertThat(rib.mergeRouteGetDelta(route1), equalTo(RibDelta.adding(route1)));
    assertThat(rib.mergeRouteGetDelta(route21), equalTo(RibDelta.adding(route21)));
    assertThat(rib.mergeRouteGetDelta(route22), equalTo(RibDelta.empty()));
    assertThat(rib.getTypedRoutes(), containsInAnyOrder(route1, route21));
    assertThat(rib.getTypedBackupRoutes(), containsInAnyOrder(route1, route21, route22));
  }

  @Test
  public void testMergeAndRemove() {
    EvpnMasterRib<EvpnType5Route> rib =
        new EvpnMasterRib<>(BgpTieBreaker.ROUTER_ID, null, false, NO_PREFERENCE);
    EvpnType5Route route =
        EvpnType5Route.builder()
            .setNetwork(Prefix.ZERO)
            .setNextHop(NextHopIp.of(Ip.parse("10.0.0.1")))
            .setProtocol(RoutingProtocol.BGP)
            .setOriginType(OriginType.IGP)
            .setOriginatorIp(Ip.ZERO)
            .setRouteDistinguisher(RouteDistinguisher.from(1, 1L))
            .setLocalPreference(10)
            .build();

    // Route should be added.
    assertThat(rib.mergeRouteGetDelta(route), equalTo(RibDelta.adding(route)));
    // Route should be removed.
    assertThat(
        rib.removeRouteGetDelta(route),
        equalTo(RibDelta.of(RouteAdvertisement.withdrawing(route))));
    // No routes should be present following removeal.
    assertThat(rib.getTypedRoutes(), empty());
    assertThat(rib.getTypedBackupRoutes(), empty());
  }
}
