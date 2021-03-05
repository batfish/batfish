package org.batfish.representation.cisco;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetDifference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetUnion;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.RouteTargetExtendedCommunities;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.statement.Statement;

@ParametersAreNonnullByDefault
public class RouteMapSetExtcommunityRtLine extends RouteMapSetLine {

  public RouteMapSetExtcommunityRtLine(Iterable<ExtendedCommunity> communities) {
    _communities = ImmutableList.copyOf(communities);
  }

  @Override
  public void applyTo(
      List<Statement> statements, CiscoConfiguration cc, Configuration c, Warnings w) {
    // Retain every community thing that is not a route-target.
    // Then replace the route-target communities with the ones from this line.
    statements.add(
        new SetCommunities(
            CommunitySetUnion.of(
                new CommunitySetDifference(
                    InputCommunities.instance(), RouteTargetExtendedCommunities.instance()),
                new LiteralCommunitySet(CommunitySet.of(_communities)))));
  }

  public List<ExtendedCommunity> getCommunities() {
    return _communities;
  }

  private List<ExtendedCommunity> _communities;
}
