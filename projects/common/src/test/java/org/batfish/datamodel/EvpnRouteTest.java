package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.junit.Test;

/** Tests of {@link EvpnRoute} */
public class EvpnRouteTest {
  @Test
  public void testGetRouteTarget() {
    EvpnType2Route.Builder builder =
        EvpnType2Route.builder()
            .setIp(Ip.parse("1.1.1.1"))
            .setMacAddress(MacAddress.parse("00:11:22:33:44:55"))
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHop(NextHopDiscard.instance())
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.1")))
            .setRouteDistinguisher(RouteDistinguisher.from(Ip.parse("1.1.1.1"), 2))
            .setVni(1);

    assertThat(builder.build().getRouteTargets(), empty());
    assertThat(
        builder
            .setCommunities(ImmutableSet.of(StandardCommunity.of(1, 1)))
            .build()
            .getRouteTargets(),
        empty());
    assertThat(
        builder
            .setCommunities(ImmutableSet.of(ExtendedCommunity.of(1, 1, 1)))
            .build()
            .getRouteTargets(),
        empty());
    assertThat(
        builder
            .setCommunities(ImmutableSet.of(ExtendedCommunity.target(1, 1)))
            .build()
            .getRouteTargets(),
        equalTo(ImmutableSet.of(ExtendedCommunity.target(1, 1))));
    assertThat(
        builder
            .setCommunities(
                ImmutableSet.of(ExtendedCommunity.target(1, 1), StandardCommunity.of(1, 1)))
            .build()
            .getRouteTargets(),
        equalTo(ImmutableSet.of(ExtendedCommunity.target(1, 1))));
    assertThat(
        builder
            .setCommunities(
                ImmutableSet.of(
                    ExtendedCommunity.target(1, 1),
                    StandardCommunity.of(1, 1),
                    ExtendedCommunity.target(2, 2)))
            .build()
            .getRouteTargets(),
        equalTo(ImmutableSet.of(ExtendedCommunity.target(1, 1), ExtendedCommunity.target(2, 2))));
  }
}
