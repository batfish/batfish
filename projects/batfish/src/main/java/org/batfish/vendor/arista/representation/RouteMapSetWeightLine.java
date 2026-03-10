package org.batfish.vendor.arista.representation;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.statement.SetWeight;
import org.batfish.datamodel.routing_policy.statement.Statement;

/** Represents a {@code `set weight [n]`} line in a route-map. */
public class RouteMapSetWeightLine extends RouteMapSetLine {

  private int _weight;

  public RouteMapSetWeightLine(int weight) {
    _weight = weight;
  }

  @Override
  public void applyTo(
      List<Statement> statements, AristaConfiguration cc, Configuration c, Warnings w) {
    statements.add(new SetWeight(new LiteralInt(_weight)));
  }
}
