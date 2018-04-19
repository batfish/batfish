package org.batfish.representation.cisco;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.NextHopExpr;
import org.batfish.datamodel.routing_policy.expr.SelfNextHop;

public class RoutePolicyNextHopSelf extends RoutePolicyNextHop {

  private static final long serialVersionUID = 1L;

  @Override
  public NextHopExpr toNextHopExpr(CiscoConfiguration cc, Configuration c, Warnings w) {
    return SelfNextHop.getInstance();
  }
}
