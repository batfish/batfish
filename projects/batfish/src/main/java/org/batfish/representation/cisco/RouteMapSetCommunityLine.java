package org.batfish.representation.cisco;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.communities.AllStandardCommunities;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetDifference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetUnion;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RouteMapSetCommunityLine extends RouteMapSetLine {

  private List<Long> _communities;

  public RouteMapSetCommunityLine(List<Long> communities) {
    _communities = communities;
  }

  @Override
  public void applyTo(
      List<Statement> statements, CiscoConfiguration cc, Configuration c, Warnings w) {
    CommunitySetExpr communities =
        new LiteralCommunitySet(
            CommunitySet.of(
                _communities.stream()
                    .map(StandardCommunity::of)
                    .collect(ImmutableList.toImmutableList())));
    statements.add(
        new SetCommunities(
            CommunitySetUnion.of(
                new CommunitySetDifference(
                    InputCommunities.instance(), AllStandardCommunities.instance()),
                communities)));
  }

  public List<Long> getCommunities() {
    return _communities;
  }
}
