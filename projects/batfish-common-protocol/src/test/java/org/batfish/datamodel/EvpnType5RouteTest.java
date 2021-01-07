package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.EvpnType5Route.Builder;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link EvpnType5Route} */
public class EvpnType5RouteTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testJavaSerialization() {
    EvpnType5Route er =
        EvpnType5Route.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopInterface("blah")
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setRouteDistinguisher(RouteDistinguisher.from(Ip.parse("1.1.1.1"), 2))
            .build();
    assertThat(SerializationUtils.clone(er), equalTo(er));
  }

  @Test
  public void testJsonSerialization() {
    EvpnType5Route er =
        EvpnType5Route.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopInterface("blah")
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setRouteDistinguisher(RouteDistinguisher.from(Ip.parse("1.1.1.1"), 2))
            .build();
    assertThat(BatfishObjectMapper.clone(er, EvpnType5Route.class), equalTo(er));
  }

  @Test
  public void testToBuilder() {
    EvpnType5Route er =
        EvpnType5Route.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopInterface("blah")
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setRouteDistinguisher(RouteDistinguisher.from(Ip.parse("1.1.1.1"), 2))
            .build();
    assertThat(er, equalTo(er.toBuilder().build()));
  }

  @Test
  public void testEquals() {
    Builder erb =
        EvpnType5Route.builder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHop(NextHopDiscard.instance())
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP)
            .setRouteDistinguisher(RouteDistinguisher.from(Ip.parse("1.1.1.1"), 2));

    new EqualsTester()
        .addEqualityGroup(erb.build(), erb.build())
        .addEqualityGroup(erb.setNetwork(Prefix.parse("1.1.2.0/24")).build())
        .addEqualityGroup(erb.setAdmin(10).build())
        .addEqualityGroup(erb.setNonRouting(true).build())
        .addEqualityGroup(erb.setNonForwarding(true).build())
        .addEqualityGroup(erb.setAsPath(AsPath.ofSingletonAsSets(1L, 1L)).build())
        .addEqualityGroup(erb.setClusterList(ImmutableSet.of(1L)).build())
        .addEqualityGroup(erb.setCommunities(ImmutableSet.of(StandardCommunity.of(1L))).build())
        .addEqualityGroup(erb.setLocalPreference(10).build())
        .addEqualityGroup(erb.setMetric(10).build())
        .addEqualityGroup(erb.setNextHopInterface("blah").build())
        .addEqualityGroup(erb.setNextHopIp(Ip.parse("2.2.2.2")).build())
        .addEqualityGroup(erb.setOriginatorIp(Ip.parse("2.2.2.2")).build())
        .addEqualityGroup(erb.setOriginType(OriginType.INCOMPLETE).build())
        .addEqualityGroup(erb.setReceivedFromIp(Ip.parse("1.1.1.1")).build())
        .addEqualityGroup(erb.setReceivedFromRouteReflectorClient(true).build())
        .addEqualityGroup(erb.setProtocol(RoutingProtocol.IBGP).build())
        .addEqualityGroup(erb.setSrcProtocol(RoutingProtocol.STATIC).build())
        .addEqualityGroup(erb.setWeight(1).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
