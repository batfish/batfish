package org.batfish.vendor.arista.representation;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
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

@ParametersAreNonnullByDefault
public class RouteMapSetCommunity extends RouteMapSetLine {

  public RouteMapSetCommunity(Iterable<StandardCommunity> communities, boolean additive) {
    _communities = ImmutableList.copyOf(communities);
    _additive = additive;
  }

  @Override
  public void applyTo(
      List<Statement> statements, AristaConfiguration cc, Configuration c, Warnings w) {
    CommunitySetExpr retainedCommunities =
        _additive
            ? InputCommunities.instance()
            : new CommunitySetDifference(
                InputCommunities.instance(), AllStandardCommunities.instance());
    statements.add(
        new SetCommunities(
            CommunitySetUnion.of(
                retainedCommunities, new LiteralCommunitySet(CommunitySet.of(_communities)))));
  }

  public boolean getAdditive() {
    return _additive;
  }

  public List<StandardCommunity> getCommunities() {
    return _communities;
  }

  private final boolean _additive;
  private @Nonnull List<StandardCommunity> _communities;
}
