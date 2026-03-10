package org.batfish.vendor.cisco_nxos.representation;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

public class StaticRouteTest {
  @Test
  public void testEquals() {
    StaticRoute.Builder builder = StaticRoute.builder().setPrefix(Prefix.parse("10.0.0.0/8"));
    new EqualsTester()
        .addEqualityGroup(builder.setDiscard(true).build(), builder.build())
        .addEqualityGroup(builder.setDiscard(false).setNextHopInterface("iface").build())
        .addEqualityGroup(builder.setNextHopIp(Ip.parse("10.1.1.1")).build())
        .addEqualityGroup(builder.setNextHopVrf("vrf").build())
        .addEqualityGroup(builder.setName("name").build())
        .addEqualityGroup(builder.setPreference(11).build())
        .addEqualityGroup(builder.setTag(55).build())
        .addEqualityGroup(builder.setTrack(3).build())
        .testEquals();
  }

  @Test
  public void testEqualsStaticRouteKey() {
    Prefix prefix = Prefix.strict("1.1.1.0/24");
    Ip nhip = Ip.parse("2.2.2.2");
    new EqualsTester()
        .addEqualityGroup(
            new StaticRoute.StaticRouteKey(prefix, true, null, null, null),
            new StaticRoute.StaticRouteKey(prefix, true, null, null, null))
        .addEqualityGroup(new StaticRoute.StaticRouteKey(prefix, false, "iface", null, null))
        .addEqualityGroup(new StaticRoute.StaticRouteKey(prefix, false, "iface", nhip, null))
        .addEqualityGroup(new StaticRoute.StaticRouteKey(prefix, false, "iface", nhip, "vrf"))
        .testEquals();
  }
}
