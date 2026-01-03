package org.batfish.common.util;

import static org.batfish.datamodel.BgpRoute.DEFAULT_LOCAL_PREFERENCE;
import static org.junit.Assert.assertEquals;

import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginMechanism;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ReceivedFromSelf;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.junit.Test;

/** Tests of {@link BgpRouteUtil}. */
public class BgpRouteUtilTest {
  @Test
  public void testConvertNonBgpRouteToBgpRoute() {
    StaticRoute inputRoute =
        StaticRoute.builder()
            .setAdministrativeCost(0)
            .setNetwork(Prefix.ZERO)
            .setMetric(0)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setRecursive(true)
            .setTag(100)
            .build();

    Bgpv4Route.Builder bgpRoute =
        BgpRouteUtil.convertNonBgpRouteToBgpRoute(
            inputRoute,
            Ip.ZERO,
            inputRoute.getNextHopIp(),
            0,
            RoutingProtocol.BGP,
            OriginMechanism.NETWORK);

    Bgpv4Route.Builder expectedRoute =
        Bgpv4Route.builder()
            .setTag(100)
            .setLocalPreference(DEFAULT_LOCAL_PREFERENCE)
            .setNetwork(Prefix.ZERO)
            .setOriginatorIp(Ip.ZERO)
            .setOriginMechanism(OriginMechanism.NETWORK)
            .setOriginType(OriginType.INCOMPLETE)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setSrcProtocol(RoutingProtocol.STATIC)
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFrom(ReceivedFromSelf.instance());

    assertEquals(bgpRoute.build(), expectedRoute.build());
  }
}
