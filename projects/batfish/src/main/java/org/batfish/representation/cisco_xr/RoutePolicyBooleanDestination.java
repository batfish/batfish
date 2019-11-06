package org.batfish.representation.cisco_xr;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork6;
import org.batfish.datamodel.routing_policy.expr.MatchPrefix6Set;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.Prefix6SetExpr;
import org.batfish.datamodel.routing_policy.expr.PrefixSetExpr;

public class RoutePolicyBooleanDestination extends RoutePolicyBoolean {

  private RoutePolicyPrefixSet _prefixSet;

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
    } else {
      Prefix6SetExpr prefix6SetExpr = _prefixSet.toPrefix6SetExpr(cc, c, w);
      return new MatchPrefix6Set(new DestinationNetwork6(), prefix6SetExpr);
    }
  }
}
