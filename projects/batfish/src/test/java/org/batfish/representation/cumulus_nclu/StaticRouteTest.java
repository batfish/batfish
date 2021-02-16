package org.batfish.representation.cumulus_nclu;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

public class StaticRouteTest {
  @Test
  public void testEquals() {
    StaticRoute sr = new StaticRoute(Prefix.ZERO, Ip.ZERO, "iface", null);
    new EqualsTester()
        .addEqualityGroup(sr, sr, new StaticRoute(Prefix.ZERO, Ip.ZERO, "iface", null))
        .addEqualityGroup(new StaticRoute(Prefix.parse("1.1.1.1/32"), Ip.ZERO, "iface", null))
        .addEqualityGroup(
            new StaticRoute(Prefix.parse("1.1.1.1/32"), Ip.parse("2.2.2.2"), "iface", null))
        .addEqualityGroup(
            new StaticRoute(Prefix.parse("1.1.1.1/32"), Ip.parse("2.2.2.2"), "otherIface", null))
        .addEqualityGroup(
            new StaticRoute(Prefix.parse("1.1.1.1/32"), Ip.parse("2.2.2.2"), "iface", 250))
        .addEqualityGroup(new Object())
        .testEquals();
  }
}
