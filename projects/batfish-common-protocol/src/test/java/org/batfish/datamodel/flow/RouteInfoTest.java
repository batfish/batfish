package org.batfish.datamodel.flow;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.junit.Test;

/** Tests of {@link RouteInfo}. */
public class RouteInfoTest {
  @Test
  public void testJsonSerialization() {
    RouteInfo orig =
        new RouteInfo(
            RoutingProtocol.BGP, Prefix.parse("1.1.1.1/30"), NextHopDiscard.instance(), 0, 1);
    RouteInfo clone = BatfishObjectMapper.clone(orig, RouteInfo.class);
    assertEquals(orig, clone);
  }

  @Test
  public void testEquals() {
    RoutingProtocol proto1 = RoutingProtocol.BGP;
    RoutingProtocol proto2 = RoutingProtocol.STATIC;
    Prefix p1 = Prefix.parse("1.1.1.1/32");
    Prefix p2 = Prefix.parse("2.2.2.2/32");
    int ad1 = 0;
    int ad2 = 1;
    int met1 = 0;
    int met2 = 1;

    new EqualsTester()
        .addEqualityGroup(
            new RouteInfo(proto1, p1, NextHopDiscard.instance(), ad1, met1),
            new RouteInfo(proto1, p1, NextHopDiscard.instance(), ad1, met1))
        .addEqualityGroup(new RouteInfo(proto2, p1, NextHopDiscard.instance(), ad1, met1))
        .addEqualityGroup(new RouteInfo(proto1, p2, NextHopDiscard.instance(), ad1, met1))
        .addEqualityGroup(new RouteInfo(proto1, p1, NextHopIp.of(Ip.parse("1.1.1.1")), ad1, met1))
        .addEqualityGroup(new RouteInfo(proto1, p1, NextHopDiscard.instance(), ad2, met1))
        .addEqualityGroup(new RouteInfo(proto1, p1, NextHopDiscard.instance(), ad1, met2))
        .testEquals();
  }
}
