package org.batfish.representation.cisco_xr;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;

public class RoutePolicyBooleanAnd extends RoutePolicyBoolean {

  private RoutePolicyBoolean _left;

  private RoutePolicyBoolean _right;

  public RoutePolicyBooleanAnd(RoutePolicyBoolean left, RoutePolicyBoolean right) {
    _left = left;
    _right = right;
  }

  public RoutePolicyBoolean getLeft() {
    return _left;
  }

  public RoutePolicyBoolean getRight() {
    return _right;
  }

  @Override
  public BooleanExpr toBooleanExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    Conjunction conj = new Conjunction();
    BooleanExpr left = _left.toBooleanExpr(cc, c, w);
    BooleanExpr right = _right.toBooleanExpr(cc, c, w);
    List<BooleanExpr> conjuncts = conj.getConjuncts();
    conjuncts.add(left);
    conjuncts.add(right);
    return conj.simplify();
  }
}
