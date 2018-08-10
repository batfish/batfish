package org.batfish.representation.cisco;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.statement.SetCommunity;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RouteMapSetCommunityLine extends RouteMapSetLine {

  private static final long serialVersionUID = 1L;

  private List<Long> _communities;

  public RouteMapSetCommunityLine(List<Long> communities) {
    _communities = communities;
  }

  @Override
  public void applyTo(
      List<Statement> statements, CiscoConfiguration cc, Configuration c, Warnings w) {
    statements.add(new SetCommunity(new LiteralCommunitySet(_communities)));
  }

  public List<Long> getCommunities() {
    return _communities;
  }

  @Override
  public RouteMapSetType getType() {
    return RouteMapSetType.COMMUNITY;
  }
}
