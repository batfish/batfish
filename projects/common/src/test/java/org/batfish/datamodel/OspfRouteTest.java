package org.batfish.datamodel;

import static org.junit.Assert.assertNotNull;

import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.route.nh.NextHopVrf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link OspfRoute} */
public final class OspfRouteTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testOspfRouteValid() {
    assertNotNull(testBuilder().setNextHop(NextHopDiscard.instance()).build());
    assertNotNull(
        testBuilder().setNextHop(NextHopInterface.of("e0", Ip.parse("192.0.2.1"))).build());
    assertNotNull(testBuilder().setNextHop(NextHopVrf.of("v1")).build());

    // non-routing
    assertNotNull(
        testBuilder().setNextHop(NextHopIp.of(Ip.parse("192.0.2.1"))).setNonRouting(true).build());
  }

  @Test
  public void testOspfRouteInvalidNextHopIpRouting() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("OSPF routes cannot only have next-hop IP unless they are non-routing.");
    testBuilder().setNextHop(NextHopIp.of(Ip.parse("192.0.2.1"))).build();
  }

  @Test
  public void testOspfRouteInvalidNextHopInterfaceWithoutNextHopIp() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("OSPF routes with next-hop interface must have next-hop IP.");
    testBuilder().setNextHop(NextHopInterface.of("e0")).build();
  }

  private static OspfIntraAreaRoute.Builder testBuilder() {
    return OspfIntraAreaRoute.builder().setArea(0).setNetwork(Prefix.ZERO);
  }
}
