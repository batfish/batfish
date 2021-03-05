package org.batfish.representation.cisco;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetUnion;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.statement.Statement;

@ParametersAreNonnullByDefault
public class RouteMapSetExtcommunityRtAdditiveLine extends RouteMapSetLine {

  public RouteMapSetExtcommunityRtAdditiveLine(Iterable<ExtendedCommunity> communities) {
    _communities = ImmutableList.copyOf(communities);
  }

  @Override
  public void applyTo(
      List<Statement> statements, CiscoConfiguration cc, Configuration c, Warnings w) {
    statements.add(
        new SetCommunities(
            CommunitySetUnion.of(
                InputCommunities.instance(),
                new LiteralCommunitySet(CommunitySet.of(_communities)))));
  }

  public List<ExtendedCommunity> getCommunities() {
    return _communities;
  }

  private List<ExtendedCommunity> _communities;
}
