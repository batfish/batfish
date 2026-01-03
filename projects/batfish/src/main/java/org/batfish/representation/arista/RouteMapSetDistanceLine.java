package org.batfish.representation.arista;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.LiteralAdministrativeCost;
import org.batfish.datamodel.routing_policy.statement.SetAdministrativeCost;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RouteMapSetDistanceLine extends RouteMapSetLine {

  private int _distance;

  public RouteMapSetDistanceLine(int distance) {
    _distance = distance;
  }

  @Override
  public void applyTo(
      List<Statement> statements, AristaConfiguration cc, Configuration c, Warnings w) {
    statements.add(new SetAdministrativeCost(new LiteralAdministrativeCost(_distance)));
  }

  public int getDistance() {
    return _distance;
  }
}
