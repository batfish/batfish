package org.batfish.representation.cisco;

import java.util.List;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.common.Warnings;

public class RouteMapSetMetricLine extends RouteMapSetLine {

   private static final long serialVersionUID = 1L;

   private IntExpr _metric;

   public RouteMapSetMetricLine(IntExpr metric) {
      _metric = metric;
   }

   @Override
   public void applyTo(List<Statement> statements, CiscoConfiguration cc,
         Configuration c, Warnings w) {
      statements.add(new SetMetric(_metric));
   }

   public IntExpr getMetric() {
      return _metric;
   }

   @Override
   public RouteMapSetType getType() {
      return RouteMapSetType.METRIC;
   }

}
