package org.batfish.vendor.cisco_nxos.representation;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Prefix6;
import org.junit.Test;

public class StaticRouteV6Test {
  @Test
  public void testEquals() {
    StaticRouteV6.Builder builder = StaticRouteV6.builder(Prefix6.parse("::1/128"));
    new EqualsTester()
        .addEqualityGroup(builder.setDiscard(true).build(), builder.build())
        .addEqualityGroup(builder.setDiscard(false).setNextHopInterface("iface").build())
        .addEqualityGroup(builder.setNextHopIp(Ip6.parse("::2")).build())
        .addEqualityGroup(builder.setNextHopVrf("vrf").build())
        .addEqualityGroup(builder.setName("name").build())
        .addEqualityGroup(builder.setPreference(11).build())
        .addEqualityGroup(builder.setTag(55).build())
        .addEqualityGroup(builder.setTrack(3).build())
        .testEquals();
  }
}
