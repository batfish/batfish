package org.batfish.representation.cisco;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.statement.SetWeight;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RoutePolicySetWeight extends RoutePolicySetStatement {

  private static final long serialVersionUID = 1L;

  private IntExpr _weight;

  public RoutePolicySetWeight(IntExpr weight) {
    _weight = weight;
  }

  @Override
  protected Statement toSetStatement(CiscoConfiguration cc, Configuration c, Warnings w) {
    return new SetWeight(_weight);
  }
}
