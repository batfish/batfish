package org.batfish.representation.arista;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.statement.AddCommunity;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RouteMapSetAdditiveCommunityLine extends RouteMapSetLine {

  private List<StandardCommunity> _communities;

  public RouteMapSetAdditiveCommunityLine(List<StandardCommunity> communities) {
    _communities = communities;
  }

  @Override
  public void applyTo(
      List<Statement> statements, AristaConfiguration cc, Configuration c, Warnings w) {
    statements.add(new AddCommunity(new LiteralCommunitySet(_communities)));
  }

  public List<StandardCommunity> getCommunities() {
    return _communities;
  }
}
