package org.batfish.datamodel.questions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginMechanism;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.questions.BgpRoute.Builder;
import org.junit.Test;

/** Test for {@link BgpRoute} */
public class BgpRouteTest {

  @Test
  public void testJsonSerialization() {
    BgpRoute br =
        BgpRoute.builder()
            .setClusterList(ImmutableSet.of(0L))
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setPathId(5)
            .setProtocol(RoutingProtocol.BGP)
            .setTunnelEncapsulationAttribute(new TunnelEncapsulationAttribute(Ip.parse("1.1.1.1")))
            .build();
    assertThat(BatfishObjectMapper.clone(br, BgpRoute.class), equalTo(br));
  }

  @Test
  public void testEquals() {
    Builder brb =
        BgpRoute.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP);
    new EqualsTester()
        .addEqualityGroup(brb.build(), brb.build())
        .addEqualityGroup(brb.setNetwork(Prefix.parse("1.1.2.0/24")).build())
        .addEqualityGroup(brb.setAsPath(AsPath.ofSingletonAsSets(1L, 1L)).build())
        .addEqualityGroup(brb.setClusterList(ImmutableSet.of(1L)).build())
        .addEqualityGroup(brb.setCommunities(ImmutableSet.of(StandardCommunity.of(1L))).build())
        .addEqualityGroup(brb.setLocalPreference(10).build())
        .addEqualityGroup(brb.setMetric(10).build())
        .addEqualityGroup(brb.setNextHopIp(Ip.parse("2.2.2.2")).build())
        .addEqualityGroup(brb.setOriginatorIp(Ip.parse("2.2.2.2")).build())
        .addEqualityGroup(brb.setOriginMechanism(OriginMechanism.NETWORK).build())
        .addEqualityGroup(brb.setOriginType(OriginType.INCOMPLETE).build())
        .addEqualityGroup(brb.setPathId(5).build())
        .addEqualityGroup(brb.setProtocol(RoutingProtocol.IBGP).build())
        .addEqualityGroup(brb.setSrcProtocol(RoutingProtocol.STATIC).build())
        .addEqualityGroup(
            brb.setTunnelEncapsulationAttribute(
                new TunnelEncapsulationAttribute(Ip.parse("1.1.1.1"))))
        .addEqualityGroup(brb.setWeight(1).build(), brb.build().toBuilder().build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testToBuilder() {
    BgpRoute br =
        BgpRoute.builder()
            .setAsPath(AsPath.ofSingletonAsSets(1L, 1L))
            .setClusterList(ImmutableSet.of(0L, 1L))
            .setCommunities(ImmutableSet.of(StandardCommunity.of(1L)))
            .setLocalPreference(10)
            .setMetric(10)
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setPathId(5)
            .setProtocol(RoutingProtocol.BGP)
            .setSrcProtocol(RoutingProtocol.STATIC)
            .setTunnelEncapsulationAttribute(new TunnelEncapsulationAttribute(Ip.parse("1.1.1.1")))
            .setWeight(1)
            .build();
    assertThat(br, equalTo(br.toBuilder().build()));
  }
}
