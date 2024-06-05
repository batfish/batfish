package org.batfish.dataplane.rib;

import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.NO_PREFERENCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.EvpnType5Route;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginMechanism;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ReceivedFromIp;
import org.batfish.datamodel.ReceivedFromSelf;
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
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setOriginatorIp(Ip.ZERO)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("10.0.0.1")))
            .setVni(1);
    EvpnType5Route route1 =
        rb.setRouteDistinguisher(RouteDistinguisher.from(1, 1L)).setLocalPreference(10).build();
    EvpnType5Route route21 =
        rb.setRouteDistinguisher(RouteDistinguisher.from(2, 2L)).setLocalPreference(20).build();
    EvpnType5Route route22 =
        rb.setRouteDistinguisher(RouteDistinguisher.from(2, 2L))
            .setLocalPreference(15)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("10.0.0.2")))
            .build();

    // route1 and route21 should be best. route22 should be backup
    assertThat(rib.mergeRouteGetDelta(route1), equalTo(RibDelta.adding(route1)));
    assertThat(rib.mergeRouteGetDelta(route21), equalTo(RibDelta.adding(route21)));
    assertThat(rib.mergeRouteGetDelta(route22), equalTo(RibDelta.empty()));
    assertThat(rib.getRoutes(), containsInAnyOrder(route1, route21));
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
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setOriginatorIp(Ip.ZERO)
            .setReceivedFrom(ReceivedFromSelf.instance())
            .setRouteDistinguisher(RouteDistinguisher.from(1, 1L))
            .setLocalPreference(10)
            .setVni(1)
            .build();

    // Route should be added.
    assertThat(rib.mergeRouteGetDelta(route), equalTo(RibDelta.adding(route)));
    // Route should be removed.
    assertThat(
        rib.removeRouteGetDelta(route),
        equalTo(RibDelta.of(RouteAdvertisement.withdrawing(route))));
    // No routes should be present following removeal.
    assertThat(rib.getRoutes(), empty());
    assertThat(rib.getTypedBackupRoutes(), empty());
  }
}
