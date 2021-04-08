package org.batfish.representation.cisco_asa;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetUnion;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RouteMapSetAdditiveCommunityLine extends RouteMapSetLine {

  private List<StandardCommunity> _communities;

  public RouteMapSetAdditiveCommunityLine(List<StandardCommunity> communities) {
    _communities = communities;
  }

  @Override
  public void applyTo(
      List<Statement> statements, AsaConfiguration cc, Configuration c, Warnings w) {
    CommunitySetExpr communities = new LiteralCommunitySet(CommunitySet.of(_communities));
    statements.add(
        new SetCommunities(CommunitySetUnion.of(InputCommunities.instance(), communities)));
  }

  public List<StandardCommunity> getCommunities() {
    return _communities;
  }
}
