package org.batfish.representation.cisco_xr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import java.util.Optional;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.routing_policy.statement.Comment;
import org.junit.Test;

public class RoutePolicyNextHopIP6Test {
  @Test
  public void testToNextHop() {
    CiscoXrConfiguration vs = new CiscoXrConfiguration();
    vs.setHostname("xr");
    Configuration vi = new Configuration("xr", ConfigurationFormat.CISCO_IOS_XR);
    Warnings w = new Warnings();
    RoutePolicyNextHopIP6 ip6 = new RoutePolicyNextHopIP6(Ip6.ZERO);
    assertThat(ip6.toNextHopExpr(vs, vi, w), equalTo(Optional.empty()));

    RoutePolicySetNextHop setIp6 = new RoutePolicySetNextHop(ip6, false);
    assertThat(setIp6.toSetStatement(vs, vi, w), instanceOf(Comment.class));
  }
}
