package org.batfish.representation.cisco;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.DiscardNextHop;
import org.batfish.datamodel.routing_policy.expr.NextHopExpr;

public class RoutePolicyNextHopDiscard extends RoutePolicyNextHop {

  @Override
  public NextHopExpr toNextHopExpr(CiscoConfiguration cc, Configuration c, Warnings w) {
    return DiscardNextHop.INSTANCE;
  }
}
