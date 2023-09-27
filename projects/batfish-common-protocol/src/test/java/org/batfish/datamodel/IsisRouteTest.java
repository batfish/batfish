package org.batfish.datamodel;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.isis.IsisLevel;
import org.junit.Test;

/** Tests of {@link IsisRoute} */
public class IsisRouteTest {
  @Test
  public void testEqualsAndToBuilder() {
    IsisRoute.Builder b =
        new IsisRoute.Builder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setLevel(IsisLevel.LEVEL_1)
            .setArea("0")
            .setNextHopIp(Ip.parse("2.2.2.2"))
            .setProtocol(RoutingProtocol.ISIS_L1)
            .setSystemId("id");

    IsisRoute original = b.build();
    IsisRoute copy = original.toBuilder().build();
    IsisRoute differentAdmin = original.toBuilder().setAdmin(90).build();
    IsisRoute differentMetric = original.toBuilder().setMetric(original.getMetric() + 50).build();
    IsisRoute differentNetwork = original.toBuilder().setNetwork(Prefix.parse("1.1.1.1/8")).build();
    IsisRoute differentNextHop = original.toBuilder().setNextHopIp(Ip.parse("3.3.3.3")).build();

    new EqualsTester()
        .addEqualityGroup(original, copy)
        .addEqualityGroup(differentAdmin)
        .addEqualityGroup(differentMetric)
        .addEqualityGroup(differentNetwork)
        .addEqualityGroup(differentNextHop)
        .testEquals();
  }
}
