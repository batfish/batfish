package org.batfish.datamodel;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link StaticRoute} */
public class StaticRouteTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();
  StaticRoute.Builder _srBuilder;

  @Test
  public void checkAllAttrs() {
    _srBuilder
        .setNextHopIp(new Ip("192.168.1.1"))
        .setNetwork(Prefix.ZERO)
        .setNextHopInterface("192.168.1.2")
        .setAdministrativeCost(1)
        .setTag(0);
    StaticRoute sr = _srBuilder.build();
    assertThat(sr.getNextHopIp(), is(new Ip("192.168.1.1")));
    assertThat(sr.getNetwork(), is(Prefix.ZERO));
    assertThat(sr.getNextHopInterface(), is("192.168.1.2"));
    assertThat(sr.getAdministrativeCost(), is(1));
    assertThat(sr.getTag(), is(0));
  }

  @Test
  public void checkNullNextHop() {
    _srBuilder.setNetwork(Prefix.ZERO).setNextHopIp(null);
    StaticRoute sr = _srBuilder.build();
    assertThat(sr.getNextHopIp(), is(Route.UNSET_ROUTE_NEXT_HOP_IP));
  }

  @Test
  public void checkNullable() {
    String errorMessage = "nextHopInterface cannot be null in StaticRoute";
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(errorMessage);
    _srBuilder.setNetwork(Prefix.ZERO);
    _srBuilder.setNextHopInterface(null);
  }

  @Test
  public void checkDefaults() {
    StaticRoute.Builder srBuilderTest = new StaticRoute.Builder();
    srBuilderTest.setNetwork(Prefix.ZERO);
    StaticRoute sr = srBuilderTest.build();
    assertThat(sr.getNextHopInterface(), is(Route.UNSET_NEXT_HOP_INTERFACE));
    assertThat(sr.getNextHopIp(), is(Route.UNSET_ROUTE_NEXT_HOP_IP));
    assertThat(sr.getAdministrativeCost(), is(Route.UNSET_ROUTE_ADMIN));
    assertThat(sr.getTag(), is(Route.UNSET_ROUTE_TAG));
  }

  @Test
  public void checkSerialization() {
    BatfishObjectMapper mapper =
        new BatfishObjectMapper(Thread.currentThread().getContextClassLoader());
    _srBuilder
        .setNextHopIp(new Ip("192.168.1.1"))
        .setNetwork(Prefix.ZERO)
        .setNextHopInterface("192.168.1.2")
        .setAdministrativeCost(1)
        .setTag(0);
    StaticRoute sr = _srBuilder.build();
    try {
      String json = mapper.writeValueAsString(sr);
      StaticRoute parsedObj = mapper.readValue(json, StaticRoute.class);
      assertThat(parsedObj.getNextHopIp(), is(new Ip("192.168.1.1")));
      assertThat(parsedObj.getNetwork(), is(Prefix.ZERO));
      assertThat(parsedObj.getNextHopInterface(), is("192.168.1.2"));
      assertThat(parsedObj.getAdministrativeCost(), is(1));
      assertThat(parsedObj.getTag(), is(0));

    } catch (IOException e) {
      throw new BatfishException(
          "Cannot parse the json to StaticRoute object", e);
    }
  }

  @Before
  public void setUp() {
    _srBuilder = new StaticRoute.Builder();
  }
}
