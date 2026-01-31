package org.batfish.vendor.arista.representation;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.communities.CommunityIn;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetDifference;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RouteMapSetCommunityDelete extends RouteMapSetLine {

  private final List<StandardCommunity> _communities;

  public RouteMapSetCommunityDelete(Iterable<StandardCommunity> communities) {
    _communities = ImmutableList.copyOf(communities);
  }

  @Override
  public void applyTo(
      List<Statement> statements, AristaConfiguration cc, Configuration c, Warnings w) {
    statements.add(
        new SetCommunities(
            new CommunitySetDifference(
                InputCommunities.instance(),
                new CommunityIn(new LiteralCommunitySet(CommunitySet.of(_communities))))));
  }

  public List<StandardCommunity> getCommunities() {
    return _communities;
  }
}
