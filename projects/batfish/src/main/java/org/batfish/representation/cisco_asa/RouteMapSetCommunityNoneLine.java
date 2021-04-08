package org.batfish.representation.cisco_asa;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RouteMapSetCommunityNoneLine extends RouteMapSetLine {

  @Override
  public void applyTo(
      List<Statement> statements, AsaConfiguration cc, Configuration c, Warnings w) {
    statements.add(new SetCommunities(new LiteralCommunitySet(CommunitySet.empty())));
  }
}
