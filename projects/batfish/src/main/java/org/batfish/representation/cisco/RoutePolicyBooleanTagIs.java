package org.batfish.representation.cisco;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.expr.MatchTag;

public class RoutePolicyBooleanTagIs extends RoutePolicyBoolean {

  /** */
  private static final long serialVersionUID = 1L;

  private IntComparator _cmp;

  private final IntExpr _expr;

  public RoutePolicyBooleanTagIs(IntComparator cmp, IntExpr expr) {
    _cmp = cmp;
    _expr = expr;
  }

  public IntExpr getValue() {
    return _expr;
  }

  @Override
  public BooleanExpr toBooleanExpr(CiscoConfiguration cc, Configuration c, Warnings w) {
    return new MatchTag(_cmp, _expr);
  }
}
