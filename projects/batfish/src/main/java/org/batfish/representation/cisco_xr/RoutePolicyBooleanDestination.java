package org.batfish.representation.cisco_xr;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.PrefixSetExpr;

public class RoutePolicyBooleanDestination extends RoutePolicyBoolean {

  private final RoutePolicyPrefixSet _prefixSet;

  public RoutePolicyBooleanDestination(RoutePolicyPrefixSet prefixSet) {
    _prefixSet = prefixSet;
  }

  public RoutePolicyPrefixSet getPrefixSet() {
    return _prefixSet;
  }

  @Override
  public BooleanExpr toBooleanExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    PrefixSetExpr prefixSetExpr = _prefixSet.toPrefixSetExpr(cc, c, w);
    if (prefixSetExpr != null) {
      return new MatchPrefixSet(DestinationNetwork.instance(), prefixSetExpr);
    }
    // Only a V6 PrefixSet
    return BooleanExprs.FALSE;
  }
}
