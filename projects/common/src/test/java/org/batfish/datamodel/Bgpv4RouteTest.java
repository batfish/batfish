package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Bgpv4Route.Builder;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link Bgpv4Route} */
public class Bgpv4RouteTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testJavaSerialization() {
    Bgpv4Route br =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopInterface("blah")
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setOriginType(OriginType.IGP)
            .setPathId(5)
            .setProtocol(RoutingProtocol.BGP)
            .build();
    assertThat(SerializationUtils.clone(br), equalTo(br));
  }

  @Test
  public void testJsonSerialization() {
    Bgpv4Route br =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopInterface("blah")
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setOriginType(OriginType.IGP)
            .setPathId(5)
            .setProtocol(RoutingProtocol.BGP)
            .build();
    assertThat(BatfishObjectMapper.clone(br, Bgpv4Route.class), equalTo(br));
  }

  @Test
  public void testToBuilder() {
    Bgpv4Route br =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHopInterface("blah")
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setOriginType(OriginType.IGP)
            .setPathId(5)
            .setProtocol(RoutingProtocol.BGP)
            .setTag(3L)
            .build();
    assertThat(br, equalTo(br.toBuilder().build()));
  }

  @Test
  public void testEquals() {
    Builder brb =
        Bgpv4Route.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP);
    new EqualsTester()
        .addEqualityGroup(brb.build(), brb.build())
        .addEqualityGroup(brb.setNetwork(Prefix.parse("1.1.2.0/24")).build())
        .addEqualityGroup(brb.setAdmin(10).build())
        .addEqualityGroup(brb.setNonRouting(true).build())
        .addEqualityGroup(brb.setNonForwarding(true).build())
        .addEqualityGroup(brb.setAsPath(AsPath.ofSingletonAsSets(1L, 1L)).build())
        .addEqualityGroup(brb.setClusterList(ImmutableSet.of(1L)).build())
        .addEqualityGroup(brb.setCommunities(ImmutableSet.of(StandardCommunity.of(1L))).build())
        .addEqualityGroup(brb.setLocalPreference(10).build())
        .addEqualityGroup(brb.setMetric(10).build())
        .addEqualityGroup(brb.setNextHopInterface("blah").build())
        .addEqualityGroup(brb.setNextHopIp(Ip.parse("2.2.2.2")).build())
        .addEqualityGroup(brb.setOriginatorIp(Ip.parse("2.2.2.2")).build())
        .addEqualityGroup(brb.setOriginType(OriginType.INCOMPLETE).build())
        .addEqualityGroup(brb.setPathId(5).build())
        .addEqualityGroup(brb.setReceivedFrom(ReceivedFromIp.of(Ip.parse("1.1.1.1"))).build())
        .addEqualityGroup(brb.setReceivedFromRouteReflectorClient(true).build())
        .addEqualityGroup(brb.setProtocol(RoutingProtocol.IBGP).build())
        .addEqualityGroup(brb.setSrcProtocol(RoutingProtocol.STATIC).build())
        .addEqualityGroup(brb.setTag(3L).build())
        .addEqualityGroup(brb.setWeight(1).build(), brb.build().toBuilder().build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testThrowsWithoutOriginType() {
    thrown.expect(IllegalArgumentException.class);
    Bgpv4Route.builder()
        .setNetwork(Prefix.parse("1.1.1.0/24"))
        .setNextHop(NextHopDiscard.instance())
        .setOriginatorIp(Ip.parse("1.1.1.1"))
        .setProtocol(RoutingProtocol.BGP)
        .build();
  }

  @Test
  public void testThrowsWithoutOriginatorIp() {
    thrown.expect(IllegalArgumentException.class);
    Bgpv4Route.builder()
        .setNetwork(Prefix.parse("1.1.1.0/24"))
        .setNextHop(NextHopDiscard.instance())
        .setProtocol(RoutingProtocol.BGP)
        .setOriginType(OriginType.IGP)
        .build();
  }

  @Test
  public void testThrowsWithoutProtocol() {
    thrown.expect(IllegalArgumentException.class);
    Bgpv4Route.builder()
        .setNetwork(Prefix.parse("1.1.1.0/24"))
        .setNextHop(NextHopDiscard.instance())
        .setOriginatorIp(Ip.parse("1.1.1.1"))
        .setOriginType(OriginType.IGP)
        .build();
  }
}
