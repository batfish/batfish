package org.batfish.vendor.cisco_nxos.representation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

/** Tests of {@link RedistributionPolicy}. */
public class RedistributionPolicyTest {
  @Test
  public void testConstruction() {
    RedistributionPolicy policy =
        new RedistributionPolicy(RoutingProtocolInstance.ospf("tag"), "map");
    assertThat(policy.getRouteMap(), equalTo("map"));
    assertThat(policy.getInstance(), equalTo(RoutingProtocolInstance.ospf("tag")));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(5)
        .addEqualityGroup(
            new RedistributionPolicy(RoutingProtocolInstance.direct(), "map"),
            new RedistributionPolicy(RoutingProtocolInstance.direct(), "map"))
        .addEqualityGroup(new RedistributionPolicy(RoutingProtocolInstance.direct(), "map2"))
        .addEqualityGroup(new RedistributionPolicy(RoutingProtocolInstance.staticc(), "map"))
        .testEquals();
  }
}
