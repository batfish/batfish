package org.batfish.representation.cisco_xr;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.LongExpr;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RoutePolicySetMed extends RoutePolicySetStatement {

  private LongExpr _med;

  public RoutePolicySetMed(LongExpr longExpr) {
    _med = longExpr;
  }

  public LongExpr getMed() {
    return _med;
  }

  @Override
  protected Statement toSetStatement(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return new SetMetric(_med);
  }
}
