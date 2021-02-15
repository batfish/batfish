package org.batfish.representation.cumulus;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
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

public class RouteMapSetCommunity implements RouteMapSet {

  private boolean _additive;
  private @Nonnull List<StandardCommunity> _communities;

  public RouteMapSetCommunity(Iterable<StandardCommunity> communities, boolean additive) {
    _communities = ImmutableList.copyOf(communities);
    _additive = additive;
  }

  public boolean getAdditive() {
    return _additive;
  }

  public void setAdditive(boolean additive) {
    _additive = additive;
  }

  @Nonnull
  @Override
  public Stream<Statement> toStatements(
      Configuration c, CumulusConcatenatedConfiguration vc, Warnings w) {
    CommunitySetExpr communities = new LiteralCommunitySet(CommunitySet.of(_communities));

    return Stream.of(
        new SetCommunities(
            getAdditive()
                ? CommunitySetUnion.of(InputCommunities.instance(), communities)
                : CommunitySetUnion.of(
                    new CommunitySetDifference(
                        InputCommunities.instance(), AllStandardCommunities.instance()),
                    communities)));
  }

  public @Nonnull List<StandardCommunity> getCommunities() {
    return _communities;
  }

  public void setCommunities(List<StandardCommunity> communities) {
    _communities = communities;
  }
}
