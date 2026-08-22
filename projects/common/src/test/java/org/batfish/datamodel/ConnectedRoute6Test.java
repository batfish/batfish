package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests for {@link ConnectedRoute6}. */
public final class ConnectedRoute6Test {

  @Test
  public void testProperties() {
    ConnectedRoute6 route =
        new ConnectedRoute6(
            Prefix6.parse("2001:db8:10::/64"),
            "Ethernet1",
            0,
            7L);

    assertThat(
        route.getNetwork(),
        equalTo(Prefix6.parse("2001:db8:10::/64")));
    assertThat(
        route.getNextHopInterface(),
        equalTo("Ethernet1"));
    assertThat(route.getNextHopIp(), nullValue());
    assertThat(route.getAdministrativeCost(), equalTo(0L));
    assertThat(route.getMetric(), equalTo(0L));
    assertThat(
        route.getProtocol(),
        equalTo(RoutingProtocol.CONNECTED));
    assertThat(route.getTag(), equalTo(7L));
  }

  @Test
  public void testSerialization() {
    ConnectedRoute6 route =
        new ConnectedRoute6(
            Prefix6.parse("2001:db8:20::/64"),
            "Ethernet2",
            0,
            Route.UNSET_ROUTE_TAG);

    ConnectedRoute6 clone =
        BatfishObjectMapper.clone(
            route, ConnectedRoute6.class);

    assertThat(clone, equalTo(route));
  }
}
