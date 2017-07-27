package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.MatchMetric;

public class PsFromMetric extends PsFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private final int _metric;

  public PsFromMetric(int metric) {
    _metric = metric;
  }

  public int getMetric() {
    return _metric;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    return new MatchMetric(IntComparator.EQ, new LiteralInt(_metric));
  }
}
