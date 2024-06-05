package org.batfish.datamodel;

import static org.batfish.datamodel.OriginMechanism.GENERATED;
import static org.batfish.datamodel.OriginMechanism.LEARNED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.junit.Test;

public class BgpRouteTest {
  @Test
  public void testBgpRouteAttributesJavaSerialization() {
    BgpRoute.BgpRouteAttributes attributes =
        BgpRoute.BgpRouteAttributes.create(
            AsPath.ofSingletonAsSets(1L, 1L),
            ImmutableSet.of(1L),
            CommunitySet.of(StandardCommunity.of(1L)),
            10,
            11,
            Ip.parse("1.1.1.1"),
            LEARNED,
            OriginType.IGP,
            RoutingProtocol.BGP,
            true,
            RoutingProtocol.STATIC,
            new TunnelEncapsulationAttribute(Ip.parse("1.1.1.1")),
            1);
    assertThat(SerializationUtils.clone(attributes), equalTo(attributes));
  }

  @Test
  public void testBgpRouteAttributesJsonSerialization() {
    // Test using a Bgpv4Route since BgpRouteAttributes does not have JSON tags
    Bgpv4Route br =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setAsPath(AsPath.ofSingletonAsSets(1L, 1L))
            .setClusterList(ImmutableSet.of(1L))
            .setCommunities(ImmutableSet.of(StandardCommunity.of(1L)))
            .setLocalPreference(10)
            .setMetric(11)
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setOriginMechanism(LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFromRouteReflectorClient(true)
            .setSrcProtocol(RoutingProtocol.STATIC)
            .setTunnelEncapsulationAttribute(new TunnelEncapsulationAttribute(Ip.parse("1.1.1.1")))
            .setWeight(1)
            .build();
    assertThat(BatfishObjectMapper.clone(br, Bgpv4Route.class), equalTo(br));
  }

  @Test
  public void testBgpRouteAttributesEquals() {
    // Test using a Bgpv4Route since BgpRouteAttributes does not have a builder
    Bgpv4Route.Builder brb =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setAsPath(AsPath.ofSingletonAsSets(1L, 1L))
            .setClusterList(ImmutableSet.of(1L))
            .setCommunities(ImmutableSet.of(StandardCommunity.of(1L)))
            .setLocalPreference(10)
            .setMetric(11)
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setOriginMechanism(LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFromRouteReflectorClient(true)
            .setSrcProtocol(RoutingProtocol.STATIC)
            .setTunnelEncapsulationAttribute(new TunnelEncapsulationAttribute(Ip.parse("1.1.1.1")))
            .setWeight(1);
    new EqualsTester()
        .addEqualityGroup(brb.build(), brb.build())
        .addEqualityGroup(brb.setAsPath(AsPath.ofSingletonAsSets(2L, 2L)).build())
        .addEqualityGroup(brb.setClusterList(ImmutableSet.of(2L)).build())
        .addEqualityGroup(brb.setCommunities(ImmutableSet.of(StandardCommunity.of(2L))).build())
        .addEqualityGroup(brb.setLocalPreference(12).build())
        .addEqualityGroup(brb.setMetric(13).build())
        .addEqualityGroup(brb.setOriginatorIp(Ip.parse("2.2.2.2")).build())
        .addEqualityGroup(brb.setOriginMechanism(GENERATED).build())
        .addEqualityGroup(brb.setOriginType(OriginType.INCOMPLETE).build())
        .addEqualityGroup(brb.setProtocol(RoutingProtocol.IBGP).build())
        .addEqualityGroup(brb.setReceivedFromRouteReflectorClient(false).build())
        .addEqualityGroup(brb.setSrcProtocol(RoutingProtocol.CONNECTED).build())
        .addEqualityGroup(
            brb.setTunnelEncapsulationAttribute(
                    new TunnelEncapsulationAttribute(Ip.parse("2.2.2.2")))
                .build())
        .addEqualityGroup(brb.setWeight(2).build())
        .testEquals();
  }
}
