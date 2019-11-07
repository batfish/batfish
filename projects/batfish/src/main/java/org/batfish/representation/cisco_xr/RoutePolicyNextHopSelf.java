package org.batfish.representation.cisco_xr;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.NextHopExpr;
import org.batfish.datamodel.routing_policy.expr.SelfNextHop;

public class RoutePolicyNextHopSelf extends RoutePolicyNextHop {

  @Override
  public NextHopExpr toNextHopExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return SelfNextHop.getInstance();
  }
}
