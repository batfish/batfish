package org.batfish.representation.cisco;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.LongExpr;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RoutePolicySetMed extends RoutePolicySetStatement {

  private static final long serialVersionUID = 1L;

  private LongExpr _med;

  public RoutePolicySetMed(LongExpr longExpr) {
    _med = longExpr;
  }

  public LongExpr getMed() {
    return _med;
  }

  @Override
  protected Statement toSetStatement(CiscoConfiguration cc, Configuration c, Warnings w) {
    return new SetMetric(_med);
  }
}
