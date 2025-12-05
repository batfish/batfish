package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.route.nh.NextHopVrf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link StaticRoute} */
public final class StaticRouteTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void checkAllAttrs() {
    StaticRoute sr =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.ZERO)
            .setNextHop(NextHopVrf.of("otherVrf"))
            .setAdministrativeCost(1)
            .setTag(3L)
            .setMetric(123)
            .build()
            .toBuilder()
            .build();
    assertThat(sr.getNetwork(), equalTo(Prefix.ZERO));
    assertThat(sr.getNextHop(), equalTo(NextHopVrf.of("otherVrf")));
    assertThat(sr.getAdministrativeCost(), equalTo(1L));
    assertThat(sr.getTag(), equalTo(3L));
    assertThat(sr.getMetric(), equalTo(123L));
  }

  @Test
  public void checkNullNextHop() {
    StaticRoute sr =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.ZERO)
            .setAdministrativeCost(1)
            .setNextHopIp(null)
            .setNextHopInterface("iface")
            .build();
    assertThat(sr.getNextHopIp(), equalTo(Route.UNSET_ROUTE_NEXT_HOP_IP));
  }

  @Test
  public void checkNullNextHopInterface() {
    StaticRoute sr =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.ZERO)
            .setAdministrativeCost(1)
            .setNextHopInterface(null)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .build();
    assertThat(sr.getNextHopInterface(), equalTo(Route.UNSET_NEXT_HOP_INTERFACE));
  }

  @Test
  public void checkDefaults() {
    StaticRoute sr =
        StaticRoute.builder()
            .setNextHop(NextHopDiscard.instance())
            .setNetwork(Prefix.ZERO)
            .setAdministrativeCost(1)
            .build();
    assertThat(sr.getAdministrativeCost(), equalTo(1L));
    assertThat(sr.getTag(), equalTo(Route.UNSET_ROUTE_TAG));
    assertThat(sr.getMetric(), equalTo(StaticRoute.DEFAULT_STATIC_ROUTE_METRIC));
  }

  @Test
  public void checkThrowsWithoutAdmin() {
    _thrown.expect(IllegalArgumentException.class);
    StaticRoute.builder().setNetwork(Prefix.ZERO).build();
  }

  @Test
  public void testToBuilderRoundTrip() {
    StaticRoute sr =
        StaticRoute.testBuilder()
            .setNextHopIp(Ip.parse("192.168.1.1"))
            .setNetwork(Prefix.ZERO)
            .setNextHopInterface("Ethernet0")
            .setNextHop(NextHopVrf.of("otherVrf"))
            .setAdministrativeCost(1)
            .setTag(0L)
            .setMetric(123)
            .build();

    assertThat(sr.toBuilder().build(), equalTo(sr));
  }

  @Test
  public void testEquals() {
    StaticRoute.Builder b =
        StaticRoute.testBuilder()
            .setNetwork(Prefix.parse("1.1.1.0/24"))
            .setNextHop(NextHopDiscard.instance())
            .setAdministrativeCost(1);
    new EqualsTester()
        .addEqualityGroup(b.build(), b.build())
        .addEqualityGroup(b.setNetwork(Prefix.parse("2.2.2.0/24")).build())
        .addEqualityGroup(b.setAdministrativeCost(2).build())
        .addEqualityGroup(b.setNonRouting(true).build())
        .addEqualityGroup(b.setNonForwarding(true).build())
        .addEqualityGroup(b.setMetric(3).build())
        .addEqualityGroup(b.setNextHop(NextHopIp.of(Ip.parse("2.2.2.2"))).build())
        .addEqualityGroup(b.setNextHop(NextHopVrf.of("otherVrf")).build())
        .addEqualityGroup(b.setTag(4L).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void checkJsonSerialization() {
    StaticRoute sr =
        StaticRoute.testBuilder()
            .setNextHopIp(Ip.parse("192.168.1.1"))
            .setNetwork(Prefix.ZERO)
            .setNextHopInterface("Ethernet0")
            .setNextHop(NextHopVrf.of("otherVrf"))
            .setAdministrativeCost(1)
            .setTag(0L)
            .setMetric(123)
            .build();

    assertThat(BatfishObjectMapper.clone(sr, StaticRoute.class), equalTo(sr));
  }

  @Test
  public void testJavaSerialization() {
    StaticRoute sr =
        StaticRoute.testBuilder()
            .setNextHopIp(Ip.parse("192.168.1.1"))
            .setNetwork(Prefix.ZERO)
            .setNextHopInterface("Ethernet0")
            .setNextHop(NextHopVrf.of("otherVrf"))
            .setAdministrativeCost(1)
            .setTag(0L)
            .setMetric(123)
            .build();

    assertThat(SerializationUtils.clone(sr), equalTo(sr));
  }
}
