package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link StaticRoute} */
public final class StaticRouteTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void checkAllAttrs() {
    StaticRoute sr =
        StaticRoute.builder()
            .setNextHopIp(Ip.parse("192.168.1.1"))
            .setNetwork(Prefix.ZERO)
            .setNextHopInterface("Ethernet0")
            .setNextVrf("otherVrf")
            .setAdministrativeCost(1)
            .setTag(0)
            .setMetric(123)
            .build();
    assertThat(sr.getNextHopIp(), equalTo(Ip.parse("192.168.1.1")));
    assertThat(sr.getNetwork(), equalTo(Prefix.ZERO));
    assertThat(sr.getNextHopInterface(), equalTo("Ethernet0"));
    assertThat(sr.getNextVrf(), equalTo("otherVrf"));
    assertThat(sr.getAdministrativeCost(), equalTo(1));
    assertThat(sr.getTag(), equalTo(0));
    assertThat(sr.getMetric(), equalTo(123L));
  }

  @Test
  public void checkNullNextHop() {
    StaticRoute sr =
        StaticRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setAdministrativeCost(1)
            .setNextHopIp(null)
            .build();
    assertThat(sr.getNextHopIp(), equalTo(Route.UNSET_ROUTE_NEXT_HOP_IP));
  }

  @Test
  public void checkNullNextHopInterface() {
    StaticRoute sr =
        StaticRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setAdministrativeCost(1)
            .setNextHopInterface(null)
            .build();
    assertThat(sr.getNextHopInterface(), equalTo(Route.UNSET_NEXT_HOP_INTERFACE));
  }

  @Test
  public void checkDefaults() {
    StaticRoute sr = StaticRoute.builder().setNetwork(Prefix.ZERO).setAdministrativeCost(1).build();
    assertThat(sr.getNextHopInterface(), equalTo(Route.UNSET_NEXT_HOP_INTERFACE));
    assertThat(sr.getNextHopIp(), equalTo(Route.UNSET_ROUTE_NEXT_HOP_IP));
    assertThat(sr.getNextVrf(), nullValue());
    assertThat(sr.getAdministrativeCost(), equalTo(1));
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
        StaticRoute.builder()
            .setNextHopIp(Ip.parse("192.168.1.1"))
            .setNetwork(Prefix.ZERO)
            .setNextHopInterface("Ethernet0")
            .setNextVrf("otherVrf")
            .setAdministrativeCost(1)
            .setTag(0)
            .setMetric(123)
            .build();

    assertThat(sr.toBuilder().build(), equalTo(sr));
  }

  @Test
  public void testEquals() {
    StaticRoute.Builder b =
        StaticRoute.builder().setNetwork(Prefix.parse("1.1.1.0/24")).setAdministrativeCost(1);
    new EqualsTester()
        .addEqualityGroup(b.build(), b.build())
        .addEqualityGroup(b.setNetwork(Prefix.parse("2.2.2.0/24")).build())
        .addEqualityGroup(b.setAdministrativeCost(2).build())
        .addEqualityGroup(b.setNonRouting(true).build())
        .addEqualityGroup(b.setNonForwarding(true).build())
        .addEqualityGroup(b.setMetric(3).build())
        .addEqualityGroup(b.setNextHopIp(Ip.parse("2.2.2.2")).build())
        .addEqualityGroup(b.setNextHopInterface("Ethernet0").build())
        .addEqualityGroup(b.setNextVrf("otherVrf").build())
        .addEqualityGroup(b.setTag(4).build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void checkJsonSerialization() throws IOException {
    StaticRoute sr =
        StaticRoute.builder()
            .setNextHopIp(Ip.parse("192.168.1.1"))
            .setNetwork(Prefix.ZERO)
            .setNextHopInterface("Ethernet0")
            .setNextVrf("otherVrf")
            .setAdministrativeCost(1)
            .setTag(0)
            .setMetric(123)
            .build();

    assertThat(BatfishObjectMapper.clone(sr, StaticRoute.class), equalTo(sr));
  }

  @Test
  public void testJavaSerialization() {
    StaticRoute sr =
        StaticRoute.builder()
            .setNextHopIp(Ip.parse("192.168.1.1"))
            .setNetwork(Prefix.ZERO)
            .setNextHopInterface("Ethernet0")
            .setNextVrf("otherVrf")
            .setAdministrativeCost(1)
            .setTag(0)
            .setMetric(123)
            .build();

    assertThat(SerializationUtils.clone(sr), equalTo(sr));
  }
}
