package org.batfish.datamodel;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link StaticRoute} */
public class StaticRouteTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void checkAllAttrs() {
    StaticRoute sr =
        StaticRoute.builder()
            .setNextHopIp(new Ip("192.168.1.1"))
            .setNetwork(Prefix.ZERO)
            .setNextHopInterface("192.168.1.2")
            .setAdministrativeCost(1)
            .setTag(0)
            .setMetric(123)
            .build();
    assertThat(sr.getNextHopIp(), is(new Ip("192.168.1.1")));
    assertThat(sr.getNetwork(), is(Prefix.ZERO));
    assertThat(sr.getNextHopInterface(), is("192.168.1.2"));
    assertThat(sr.getAdministrativeCost(), is(1));
    assertThat(sr.getTag(), is(0));
    assertThat(sr.getMetric(), is(123L));
  }

  @Test
  public void checkNullNextHop() {
    StaticRoute sr =
        StaticRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setAdministrativeCost(1)
            .setNextHopIp(null)
            .build();
    assertThat(sr.getNextHopIp(), is(Route.UNSET_ROUTE_NEXT_HOP_IP));
  }

  @Test
  public void checkNullNextHopInterface() {
    StaticRoute sr =
        StaticRoute.builder()
            .setNetwork(Prefix.ZERO)
            .setAdministrativeCost(1)
            .setNextHopInterface(null)
            .build();
    assertThat(sr.getNextHopInterface(), is(Route.UNSET_NEXT_HOP_INTERFACE));
  }

  @Test
  public void checkDefaults() {
    StaticRoute sr = StaticRoute.builder().setNetwork(Prefix.ZERO).setAdministrativeCost(1).build();
    assertThat(sr.getNextHopInterface(), is(Route.UNSET_NEXT_HOP_INTERFACE));
    assertThat(sr.getNextHopIp(), is(Route.UNSET_ROUTE_NEXT_HOP_IP));
    assertThat(sr.getAdministrativeCost(), is(1));
    assertThat(sr.getTag(), is(Route.UNSET_ROUTE_TAG));
    assertThat(sr.getMetric(), is(StaticRoute.DEFAULT_STATIC_ROUTE_METRIC));
  }

  @Test
  public void checkThrowsWithoutAdmin() {
    _thrown.expect(IllegalArgumentException.class);
    StaticRoute.builder().setNetwork(Prefix.ZERO).build();
  }

  @Test
  public void checkSerialization() {
    StaticRoute sr =
        StaticRoute.builder()
            .setNextHopIp(new Ip("192.168.1.1"))
            .setNetwork(Prefix.ZERO)
            .setNextHopInterface("192.168.1.2")
            .setAdministrativeCost(1)
            .setTag(0)
            .setMetric(123)
            .build();
    try {
      StaticRoute parsedObj = BatfishObjectMapper.clone(sr, StaticRoute.class);
      assertThat(parsedObj.getNextHopIp(), is(new Ip("192.168.1.1")));
      assertThat(parsedObj.getNetwork(), is(Prefix.ZERO));
      assertThat(parsedObj.getNextHopInterface(), is("192.168.1.2"));
      assertThat(parsedObj.getAdministrativeCost(), is(1));
      assertThat(parsedObj.getTag(), is(0));
      assertThat(parsedObj.getMetric(), is(123L));
    } catch (IOException e) {
      throw new BatfishException("Cannot parse the json to StaticRoute object", e);
    }
  }
}
