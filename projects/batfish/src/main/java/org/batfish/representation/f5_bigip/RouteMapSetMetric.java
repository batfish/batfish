package org.batfish.representation.f5_bigip;

import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.Statement;

/** Route-map transformation that replaces the metric attribute of the route */
@ParametersAreNonnullByDefault
public class RouteMapSetMetric implements RouteMapSet {

  private final long _metric;

  public RouteMapSetMetric(long metric) {
    _metric = metric;
  }

  public long getMetric() {
    return _metric;
  }

  @Override
  public @Nonnull Stream<Statement> toStatements(
      Configuration c, F5BigipConfiguration vc, Warnings w) {
    return Stream.of(new SetMetric(new LiteralLong(_metric)));
  }
}
