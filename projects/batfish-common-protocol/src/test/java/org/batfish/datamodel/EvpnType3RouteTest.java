package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.EvpnType3Route.Builder;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link EvpnType3Route} */
public class EvpnType3RouteTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testJavaSerialization() {
    EvpnType3Route er =
        EvpnType3Route.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopInterface("blah")
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setPathId(5)
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.1")))
            .setRouteDistinguisher(RouteDistinguisher.from(Ip.parse("1.1.1.1"), 2))
            .setVni(1)
            .setVniIp(Ip.parse("1.1.1.1"))
            .build();
    assertThat(SerializationUtils.clone(er), equalTo(er));
  }

  @Test
  public void testJsonSerialization() {
    EvpnType3Route er =
        EvpnType3Route.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopInterface("blah")
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setPathId(5)
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.1")))
            .setRouteDistinguisher(RouteDistinguisher.from(Ip.parse("1.1.1.1"), 2))
            .setVni(1)
            .setVniIp(Ip.parse("1.1.1.1"))
            .build();
    assertThat(BatfishObjectMapper.clone(er, EvpnType3Route.class), equalTo(er));
  }

  @Test
  public void testToBuilder() {
    EvpnType3Route er =
        EvpnType3Route.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopInterface("blah")
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setPathId(5)
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.1")))
            .setRouteDistinguisher(RouteDistinguisher.from(Ip.parse("1.1.1.1"), 2))
            .setVni(1)
            .setVniIp(Ip.parse("1.1.1.1"))
            .build();
    assertThat(er, equalTo(er.toBuilder().build()));
  }

  @Test
  public void testEquals() {
    Builder erb =
        EvpnType3Route.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHop(NextHopDiscard.instance())
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.1")))
            .setRouteDistinguisher(RouteDistinguisher.from(Ip.parse("1.1.1.1"), 2))
            .setVni(1)
            .setVniIp(Ip.parse("1.1.1.1"));
    new EqualsTester()
        .addEqualityGroup(erb.build(), erb.build())
        .addEqualityGroup(erb.setAsPath(AsPath.ofSingletonAsSets(1L, 1L)).build())
        .addEqualityGroup(erb.setClusterList(ImmutableSet.of(1L)).build())
        .addEqualityGroup(erb.setCommunities(ImmutableSet.of(StandardCommunity.of(1L))).build())
        .addEqualityGroup(erb.setLocalPreference(10).build())
        .addEqualityGroup(erb.setMetric(10).build())
        .addEqualityGroup(erb.setNextHopInterface("blah").build())
        .addEqualityGroup(erb.setNextHopIp(Ip.parse("2.2.2.2")).build())
        .addEqualityGroup(erb.setOriginatorIp(Ip.parse("2.2.2.2")).build())
        .addEqualityGroup(erb.setOriginMechanism(OriginMechanism.GENERATED).build())
        .addEqualityGroup(erb.setOriginType(OriginType.INCOMPLETE).build())
        .addEqualityGroup(erb.setPathId(5).build())
        .addEqualityGroup(erb.setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.2"))).build())
        .addEqualityGroup(erb.setReceivedFromRouteReflectorClient(true).build())
        .addEqualityGroup(
            erb.setRouteDistinguisher(RouteDistinguisher.from(Ip.parse("2.2.2.2"), 2)).build())
        .addEqualityGroup(erb.setVni(2).build())
        .addEqualityGroup(erb.setProtocol(RoutingProtocol.IBGP).build())
        .addEqualityGroup(erb.setSrcProtocol(RoutingProtocol.STATIC).build())
        .addEqualityGroup(erb.setWeight(1).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testBuilderNetworkOverwrite() {
    Builder erb =
        EvpnType3Route.builder()
            .setVniIp(Ip.parse("1.1.1.1"))
            .setNetwork(Prefix.parse("3.3.3.3/24"))
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHop(NextHopDiscard.instance())
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.1")))
            .setRouteDistinguisher(RouteDistinguisher.from(Ip.parse("2.2.2.2"), 2))
            .setVni(1);

    assertThat(erb.build().getNetwork(), equalTo(Prefix.parse("1.1.1.1/32")));
  }

  @Test
  public void testJsonSerializationNetworkOverwrite() {
    EvpnType3Route er =
        EvpnType3Route.builder()
            .setVniIp(Ip.parse("1.1.1.1"))
            .setNetwork(Prefix.parse("3.3.3.3/24"))
            .setNextHopInterface("blah")
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setOriginMechanism(OriginMechanism.LEARNED)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.1")))
            .setRouteDistinguisher(RouteDistinguisher.from(Ip.parse("1.2.3.4"), 2))
            .setVni(1)
            .build();

    assertThat(
        BatfishObjectMapper.clone(er, EvpnType3Route.class).getNetwork(),
        equalTo(Prefix.parse("1.1.1.1/32")));
  }
}
