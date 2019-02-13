package org.batfish.dataplane.rib;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IsisRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.isis.IsisLevel;
import org.junit.Test;

public class IsisRibTest {

  @Test
  public void testRoutePreferenceComparator() {
    IsisRoute baseRoute =
        new IsisRoute.Builder()
            .setAdmin(115)
            .setArea("0")
            .setLevel(IsisLevel.LEVEL_1)
            .setMetric(10)
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setNextHopIp(Ip.parse("2.2.2.2"))
            .setOverload(false)
            .setProtocol(RoutingProtocol.ISIS_L1)
            .setSystemId("id")
            .build();

    IsisRoute highAdmin = baseRoute.toBuilder().setAdmin(200).build();
    IsisRoute level2 = baseRoute.toBuilder().setLevel(IsisLevel.LEVEL_2).build();
    IsisRoute overloaded = baseRoute.toBuilder().setOverload(true).build();
    IsisRoute highMetric = baseRoute.toBuilder().setMetric(20).build();

    List<IsisRoute> increasingPreferenceOrder =
        ImmutableList.of(highAdmin, level2, overloaded, highMetric, baseRoute);
    for (int i = 0; i < increasingPreferenceOrder.size(); i++) {
      for (int j = 0; j < increasingPreferenceOrder.size(); j++) {
        int comparison =
            IsisRib.routePreferenceComparator.compare(
                increasingPreferenceOrder.get(i), increasingPreferenceOrder.get(j));
        assertThat(Integer.signum(comparison), equalTo(Integer.signum(i - j)));
      }
    }
  }
}
