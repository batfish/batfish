package org.batfish.representation.cisco_asa;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;

public class RouteMapSetCommunityNoneLine extends RouteMapSetLine {

  @Override
  public void applyTo(
      List<Statement> statements, AsaConfiguration cc, Configuration c, Warnings w) {
    statements.add(Statements.DeleteAllCommunities.toStaticStatement());
  }
}
