package org.batfish.representation.cisco;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.LongExpr;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RoutePolicySetIsisMetric extends RoutePolicySetStatement {

  /** */
  private static final long serialVersionUID = 1L;

  private LongExpr _metric;

  public RoutePolicySetIsisMetric(LongExpr metric) {
    _metric = metric;
  }

  @Override
  protected Statement toSetStatement(CiscoConfiguration cc, Configuration c, Warnings w) {
    return new SetMetric(_metric);
  }
}
