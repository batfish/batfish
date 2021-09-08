package org.batfish.representation.cisco_nxos;

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
}
