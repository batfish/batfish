package org.batfish.representation.cisco;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.CallExpr;

public class RoutePolicyBooleanApply extends RoutePolicyBoolean {

  /** */
  private static final long serialVersionUID = 1L;

  private String _name;

  public RoutePolicyBooleanApply(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  @Override
  public BooleanExpr toBooleanExpr(CiscoConfiguration cc, Configuration c, Warnings w) {
    return new CallExpr(_name);
  }
}
