package org.batfish.representation.cisco_xr;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.HasRoute;
import org.batfish.datamodel.routing_policy.expr.HasRoute6;
import org.batfish.datamodel.routing_policy.expr.Prefix6SetExpr;
import org.batfish.datamodel.routing_policy.expr.PrefixSetExpr;

public class RoutePolicyBooleanRibHasRoute extends RoutePolicyBoolean {

  private RoutePolicyPrefixSet _prefixSet;

  public RoutePolicyBooleanRibHasRoute(RoutePolicyPrefixSet prefixSet) {
    _prefixSet = prefixSet;
  }

  public RoutePolicyPrefixSet getPrefixSet() {
    return _prefixSet;
  }

  @Override
  public BooleanExpr toBooleanExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    PrefixSetExpr prefixSetExpr = _prefixSet.toPrefixSetExpr(cc, c, w);
    if (prefixSetExpr != null) {
      return new HasRoute(prefixSetExpr);
    } else {
      Prefix6SetExpr prefix6SetExpr = _prefixSet.toPrefix6SetExpr(cc, c, w);
      return new HasRoute6(prefix6SetExpr);
    }
  }
}
