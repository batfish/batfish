package org.batfish.question.routes;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.junit.Test;

/** Test {@link BgpRouteRowSecondaryKey} */
public class BgpRouteRowSecondaryKeyTest {
  @Test
  public void testEquals() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    NextHop nh1 = NextHopIp.of(ip1);
    NextHop nh2 = NextHopIp.of(ip2);

    BgpRouteRowSecondaryKey key = new BgpRouteRowSecondaryKey(nh1, "bgp", ip1, null);
    new EqualsTester()
        .addEqualityGroup(key, new BgpRouteRowSecondaryKey(nh1, "bgp", ip1, null))
        .addEqualityGroup(new BgpRouteRowSecondaryKey(nh2, "bgp", ip1, null))
        .addEqualityGroup(new BgpRouteRowSecondaryKey(nh1, "ibgp", ip1, null))
        .addEqualityGroup(new BgpRouteRowSecondaryKey(nh1, "bgp", ip2, null))
        .addEqualityGroup(new BgpRouteRowSecondaryKey(nh1, "bgp", ip1, 1))
        .testEquals();
  }
}
