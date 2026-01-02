package org.batfish.datamodel.questions;

import static org.batfish.datamodel.AbstractRoute.PROP_METRIC;
import static org.batfish.datamodel.BgpRoute.PROP_AS_PATH;
import static org.batfish.datamodel.BgpRoute.PROP_COMMUNITIES;
import static org.batfish.datamodel.BgpRoute.PROP_LOCAL_PREFERENCE;
import static org.batfish.datamodel.OriginMechanism.LEARNED;
import static org.batfish.datamodel.questions.BgpRoute.PROP_ADMINISTRATIVE_DISTANCE;
import static org.batfish.datamodel.questions.BgpRoute.PROP_NEXT_HOP;
import static org.batfish.datamodel.questions.BgpRoute.PROP_ORIGINATOR_IP;
import static org.batfish.datamodel.questions.BgpRoute.PROP_ORIGIN_TYPE;
import static org.batfish.datamodel.questions.BgpRoute.PROP_TAG;
import static org.batfish.datamodel.questions.BgpRoute.PROP_TUNNEL_ENCAPSULATION_ATTRIBUTE;
import static org.batfish.datamodel.questions.BgpRoute.PROP_WEIGHT;
import static org.batfish.datamodel.questions.BgpRouteDiff.routeDiffs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.answers.NextHopConcrete;
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.questions.BgpRoute.Builder;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link BgpRouteDiff}. */
public class BgpRouteDiffTest {
  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testJsonSerialization() {
    BgpRouteDiff diff = new BgpRouteDiff(PROP_AS_PATH, "A", "B");
    assertEquals(diff, BatfishObjectMapper.clone(diff, BgpRouteDiff.class));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new BgpRouteDiff(PROP_AS_PATH, "a", "b"), new BgpRouteDiff(PROP_AS_PATH, "a", "b"))
        .addEqualityGroup(new BgpRouteDiff(PROP_COMMUNITIES, "a", "b"))
        .addEqualityGroup(new BgpRouteDiff(PROP_AS_PATH, "c", "b"))
        .addEqualityGroup(new BgpRouteDiff(PROP_AS_PATH, "a", "c"))
        .testEquals();
  }

  private static Builder builder() {
    return BgpRoute.builder()
        .setNetwork(Prefix.ZERO)
        .setOriginatorIp(Ip.ZERO)
        .setOriginMechanism(LEARNED)
        .setOriginType(OriginType.IGP)
        .setProtocol(RoutingProtocol.BGP);
  }

  @Test
  public void testRouteDiffs() {
    BgpRoute route1;
    BgpRoute route2;

    // change AS path
    route1 = builder().setAsPath(AsPath.ofSingletonAsSets(1L, 2L)).build();
    route2 = builder().setAsPath(AsPath.ofSingletonAsSets(2L, 3L)).build();
    assertThat(
        routeDiffs(route1, route2).getDiffs(),
        contains(new BgpRouteDiff(PROP_AS_PATH, "[1, 2]", "[2, 3]")));

    // change communities
    route1 =
        builder()
            .setCommunities(ImmutableSet.of(StandardCommunity.of(1L), StandardCommunity.of(2L)))
            .build();
    route2 =
        builder()
            .setCommunities(ImmutableSet.of(StandardCommunity.of(2L), StandardCommunity.of(3L)))
            .build();
    assertThat(
        routeDiffs(route1, route2).getDiffs(),
        contains(new BgpRouteDiff(PROP_COMMUNITIES, "[0:1, 0:2]", "[0:2, 0:3]")));

    // change local preference
    route1 = builder().setLocalPreference(1).build();
    route2 = builder().setLocalPreference(2).build();
    assertThat(
        routeDiffs(route1, route2).getDiffs(),
        contains(new BgpRouteDiff(PROP_LOCAL_PREFERENCE, "1", "2")));

    // change metric
    route1 = builder().setMetric(1).build();
    route2 = builder().setMetric(2).build();
    assertThat(
        routeDiffs(route1, route2).getDiffs(), contains(new BgpRouteDiff(PROP_METRIC, "1", "2")));

    // change administrative distance
    route1 = builder().setAdminDist(1).build();
    route2 = builder().setAdminDist(2).build();
    assertThat(
        routeDiffs(route1, route2).getDiffs(),
        contains(new BgpRouteDiff(PROP_ADMINISTRATIVE_DISTANCE, "1", "2")));

    // change all properties
    route1 =
        builder()
            .setAdminDist(1)
            .setAsPath(AsPath.ofSingletonAsSets(1L, 2L))
            .setCommunities(ImmutableSet.of(StandardCommunity.of(1L), StandardCommunity.of(2L)))
            .setLocalPreference(1)
            .setMetric(1)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setOriginatorIp(Ip.parse("1.1.1.1"))
            .setOriginType(OriginType.IGP)
            .setTag(1)
            .setWeight(1)
            .build();
    route2 =
        builder()
            .setAdminDist(2)
            .setAsPath(AsPath.ofSingletonAsSets(2L, 3L))
            .setCommunities(ImmutableSet.of(StandardCommunity.of(2L), StandardCommunity.of(3L)))
            .setLocalPreference(2)
            .setMetric(2)
            .setNextHopIp(Ip.parse("2.2.2.2"))
            .setOriginatorIp(Ip.parse("2.2.2.2"))
            .setOriginType(OriginType.EGP)
            .setTag(2)
            .setTunnelEncapsulationAttribute(new TunnelEncapsulationAttribute(Ip.parse("2.2.2.2")))
            .setWeight(2)
            .build();
    assertThat(
        routeDiffs(route1, route2).getDiffs(),
        containsInAnyOrder(
            new BgpRouteDiff(PROP_ADMINISTRATIVE_DISTANCE, "1", "2"),
            new BgpRouteDiff(PROP_AS_PATH, "[1, 2]", "[2, 3]"),
            new BgpRouteDiff(PROP_COMMUNITIES, "[0:1, 0:2]", "[0:2, 0:3]"),
            new BgpRouteDiff(PROP_LOCAL_PREFERENCE, "1", "2"),
            new BgpRouteDiff(PROP_METRIC, "1", "2"),
            new BgpRouteDiff(
                PROP_NEXT_HOP,
                new NextHopConcrete(NextHopIp.of(Ip.parse("1.1.1.1"))).toString(),
                new NextHopConcrete(NextHopIp.of(Ip.parse("2.2.2.2"))).toString()),
            new BgpRouteDiff(PROP_ORIGINATOR_IP, "1.1.1.1", "2.2.2.2"),
            new BgpRouteDiff(PROP_ORIGIN_TYPE, "IGP", "EGP"),
            new BgpRouteDiff(PROP_TAG, "1", "2"),
            new BgpRouteDiff(
                PROP_TUNNEL_ENCAPSULATION_ATTRIBUTE,
                "null",
                "Tunnel type: ipip, remote endpoint: 2.2.2.2"),
            new BgpRouteDiff(PROP_WEIGHT, "1", "2")));
  }

  @Test
  public void testUnsupported() {
    BgpRoute r1 = builder().setProtocol(RoutingProtocol.BGP).build();
    BgpRoute r2 = builder().setProtocol(RoutingProtocol.OSPF).build();

    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("BGP");
    _thrown.expectMessage("OSPF");
    routeDiffs(r1, r2);
  }
}
