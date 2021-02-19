package org.batfish.representation.cumulus;

import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.LongExpr;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.Statement;

/** Clause of set metric in route map. */
public class RouteMapSetMetric implements RouteMapSet {

  private final LongExpr _metric;

  public RouteMapSetMetric(LongExpr metric) {
    _metric = metric;
  }

  public LongExpr getMetric() {
    return _metric;
  }

  @Nonnull
  @Override
  public Stream<Statement> toStatements(
      Configuration c, CumulusConcatenatedConfiguration vc, Warnings w) {
    return Stream.of(new SetMetric(_metric));
  }
}
