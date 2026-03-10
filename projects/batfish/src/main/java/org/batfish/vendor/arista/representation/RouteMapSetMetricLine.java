package org.batfish.vendor.arista.representation;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.LongExpr;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RouteMapSetMetricLine extends RouteMapSetLine {

  private LongExpr _metric;

  public RouteMapSetMetricLine(LongExpr metric) {
    _metric = metric;
  }

  @Override
  public void applyTo(
      List<Statement> statements, AristaConfiguration cc, Configuration c, Warnings w) {
    statements.add(new SetMetric(_metric));
  }

  public LongExpr getMetric() {
    return _metric;
  }
}
