package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.MatchMetric;

/** Represents a "from metric" line in a {@link PsTerm} */
public class PsFromMetric extends PsFrom {

  private final long _metric;

  public PsFromMetric(long metric) {
    _metric = metric;
  }

  public long getMetric() {
    return _metric;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    return new MatchMetric(IntComparator.EQ, new LiteralLong(_metric));
  }
}
