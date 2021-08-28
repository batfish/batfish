package org.batfish.representation.cisco_xr;

import com.google.common.collect.ImmutableList;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.HasRoute;
import org.batfish.datamodel.routing_policy.expr.HasRoute6;
import org.batfish.datamodel.routing_policy.expr.Prefix6SetExpr;
import org.batfish.datamodel.routing_policy.expr.PrefixSetExpr;

public class RoutePolicyBooleanRibHasRoute extends RoutePolicyBoolean {

  private final RoutePolicyPrefixSet _prefixSet;

  public RoutePolicyBooleanRibHasRoute(RoutePolicyPrefixSet prefixSet) {
    _prefixSet = prefixSet;
  }

  public RoutePolicyPrefixSet getPrefixSet() {
    return _prefixSet;
  }

  @Override
  public BooleanExpr toBooleanExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    ImmutableList.Builder<BooleanExpr> exprs = ImmutableList.builder();
    PrefixSetExpr prefixSetExpr = _prefixSet.toPrefixSetExpr(cc, c, w);
    if (prefixSetExpr != null) {
      exprs.add(new HasRoute(prefixSetExpr));
    }
    Prefix6SetExpr prefix6SetExpr = _prefixSet.toPrefix6SetExpr(cc, c, w);
    if (prefix6SetExpr != null) {
      exprs.add(new HasRoute6(prefix6SetExpr));
    }
    return new Disjunction(exprs.build());
  }
}
