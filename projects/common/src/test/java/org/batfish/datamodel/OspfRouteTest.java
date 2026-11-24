package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.route.nh.NextHopVrf;
import org.junit.jupiter.api.Test;

/** Tests of {@link OspfRoute} */
final class OspfRouteTest {

  @Test
  void testOspfRouteValid() {
    assertNotNull(testBuilder().setNextHop(NextHopDiscard.instance()).build());
    assertNotNull(
        testBuilder().setNextHop(NextHopInterface.of("e0", Ip.parse("192.0.2.1"))).build());
    assertNotNull(testBuilder().setNextHop(NextHopVrf.of("v1")).build());

    // non-routing
    assertNotNull(
        testBuilder().setNextHop(NextHopIp.of(Ip.parse("192.0.2.1"))).setNonRouting(true).build());
  }

  @Test
  void testOspfRouteInvalidNextHopIpRouting() {
    Throwable exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> testBuilder().setNextHop(NextHopIp.of(Ip.parse("192.0.2.1"))).build());
    assertThat(
        exception.getMessage(),
        containsString("OSPF routes cannot only have next-hop IP unless they are non-routing."));
  }

  @Test
  void testOspfRouteInvalidNextHopInterfaceWithoutNextHopIp() {
    Throwable exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> testBuilder().setNextHop(NextHopInterface.of("e0")).build());
    assertThat(
        exception.getMessage(),
        containsString("OSPF routes with next-hop interface must have next-hop IP."));
  }

  private static OspfIntraAreaRoute.Builder testBuilder() {
    return OspfIntraAreaRoute.builder().setArea(0).setNetwork(Prefix.ZERO);
  }
}
