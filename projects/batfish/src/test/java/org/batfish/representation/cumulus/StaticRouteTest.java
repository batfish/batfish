package org.batfish.representation.cumulus;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

public class StaticRouteTest {
  @Test
  public void testEquals() {
    StaticRoute sr = new StaticRoute(Prefix.ZERO, Ip.ZERO, "iface");
    new EqualsTester()
        .addEqualityGroup(sr, sr, new StaticRoute(Prefix.ZERO, Ip.ZERO, "iface"))
        .addEqualityGroup(new StaticRoute(Prefix.parse("1.1.1.1/32"), Ip.ZERO, "iface"))
        .addEqualityGroup(new StaticRoute(Prefix.parse("1.1.1.1/32"), Ip.parse("2.2.2.2"), "iface"))
        .addEqualityGroup(
            new StaticRoute(Prefix.parse("1.1.1.1/32"), Ip.parse("2.2.2.2"), "otherIface"))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
