package org.batfish.minesweeper.communities;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.aspath.RoutingPolicyCollector;
import org.batfish.minesweeper.utils.Tuple;

/** Collect all community literals and regexes in a routing policy. */
@ParametersAreNonnullByDefault
public class RoutePolicyStatementVarCollector extends RoutingPolicyCollector<CommunityVar> {

  @Override
  public Set<CommunityVar> visitSetCommunities(
      SetCommunities setCommunities, Tuple<Set<String>, Configuration> arg) {
    return setCommunities
        .getCommunitySetExpr()
        .accept(new CommunitySetExprVarCollector(), arg.getSecond());
  }

  @Override
  public Set<CommunityVar> visitMatchCommunities(
      MatchCommunities matchCommunities, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.<CommunityVar>builder()
        .addAll(
            matchCommunities
                .getCommunitySetExpr()
                .accept(new CommunitySetExprVarCollector(), arg.getSecond()))
        .addAll(
            matchCommunities
                .getCommunitySetMatchExpr()
                .accept(new CommunitySetMatchExprVarCollector(), arg.getSecond()))
        .build();
  }
}
