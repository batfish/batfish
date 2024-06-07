package org.batfish.minesweeper.collectors;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.minesweeper.aspath.RoutingPolicyCollector;
import org.batfish.minesweeper.utils.Tuple;

/** Collect all community-list names in a route-policy {@link Statement}. */
@ParametersAreNonnullByDefault
public class CommunityNameCollector extends RoutingPolicyCollector<String> {

  @Override
  public Set<String> visitMatchCommunities(
      MatchCommunities matchCommunities, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.<String>builder()
        .addAll(matchCommunities.getCommunitySetExpr().accept(new CommunitySetExprCollector(), arg))
        .addAll(
            matchCommunities
                .getCommunitySetMatchExpr()
                .accept(new CommunitySetMatchExprCollector(), arg))
        .build();
  }

  @Override
  public Set<String> visitSetCommunities(
      SetCommunities setCommunities, Tuple<Set<String>, Configuration> arg) {
    return setCommunities.getCommunitySetExpr().accept(new CommunitySetExprCollector(), arg);
  }
}
