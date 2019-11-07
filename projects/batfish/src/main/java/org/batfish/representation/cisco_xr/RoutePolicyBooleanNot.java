package org.batfish.representation.cisco_xr;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.Not;

public class RoutePolicyBooleanNot extends RoutePolicyBoolean {

  private RoutePolicyBoolean _operand;

  public RoutePolicyBooleanNot(RoutePolicyBoolean operand) {
    _operand = operand;
  }

  public RoutePolicyBoolean getOperand() {
    return _operand;
  }

  @Override
  public BooleanExpr toBooleanExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return new Not(_operand.toBooleanExpr(cc, c, w));
  }
}
