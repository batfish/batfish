package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests IPv6 static-route model behavior. */
public final class StaticRoute6Test {

  @Test
  public void testSerialization() {
    StaticRoute6 route =
        StaticRoute6.builder()
            .setNetwork(
                Prefix6.parse(
                    "2001:db8:100::/64"))
            .setNextHopIp(
                Ip6.parse(
                    "2001:db8:10::2"))
            .setAdministrativeCost(5)
            .setMetric(7)
            .setTag(9)
            .build();

    assertThat(
        BatfishObjectMapper.clone(
            route,
            StaticRoute6.class),
        equalTo(route));

    assertThat(
        route.getProtocol(),
        equalTo(RoutingProtocol.STATIC));
  }
}
