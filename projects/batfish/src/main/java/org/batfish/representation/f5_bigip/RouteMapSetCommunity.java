package org.batfish.representation.f5_bigip;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;
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

/** Route-map transformation that replaces the communities attribute of the route */
@ParametersAreNonnullByDefault
public final class RouteMapSetCommunity implements RouteMapSet {

  private final @Nonnull Set<Long> _communities;

  public RouteMapSetCommunity(Collection<Long> communities) {
    _communities = ImmutableSet.copyOf(communities);
  }

  public @Nonnull Set<Long> getCommunities() {
    return _communities;
  }

  @Override
  public @Nonnull Stream<Statement> toStatements(
      Configuration c, F5BigipConfiguration vc, Warnings w) {
    CommunitySetExpr communities =
        new LiteralCommunitySet(
            CommunitySet.of(
                _communities.stream()
                    .map(StandardCommunity::of)
                    .collect(ImmutableList.toImmutableList())));
    return Stream.of(
        new SetCommunities(
            CommunitySetUnion.of(
                new CommunitySetDifference(
                    InputCommunities.instance(), AllStandardCommunities.instance()),
                communities)));
  }
}
