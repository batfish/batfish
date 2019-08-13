package org.batfish.representation.cumulus;

import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.Statement;

/** Clause of set metric in route map. */
public class RouteMapSetMetric implements RouteMapSet {

  private final long _metric;

  public RouteMapSetMetric(long metric) {
    _metric = metric;
  }

  public long getMetric() {
    return _metric;
  }

  @Nonnull
  @Override
  public Stream<Statement> toStatements(Configuration c, CumulusNcluConfiguration vc, Warnings w) {
    return Stream.of(new SetMetric(new LiteralLong(_metric)));
  }
}
