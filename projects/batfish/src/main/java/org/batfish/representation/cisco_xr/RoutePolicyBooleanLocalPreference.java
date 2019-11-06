package org.batfish.representation.cisco_xr;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.expr.MatchLocalPreference;

public class RoutePolicyBooleanLocalPreference extends RoutePolicyBoolean {

  private IntComparator _cmp;

  private final IntExpr _expr;

  public RoutePolicyBooleanLocalPreference(IntComparator cmp, IntExpr expr) {
    _cmp = cmp;
    _expr = expr;
  }

  public IntExpr getValue() {
    return _expr;
  }

  @Override
  public BooleanExpr toBooleanExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return new MatchLocalPreference(_cmp, _expr);
  }
}
