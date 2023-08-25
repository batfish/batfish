package org.batfish.minesweeper.collectors;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.minesweeper.aspath.BooleanExprMatchCollector;
import org.batfish.minesweeper.utils.Tuple;

/** Collect all community-list names in a {@link BooleanExpr}. */
@ParametersAreNonnullByDefault
public class CommunityBooleanExprCollector extends BooleanExprMatchCollector<String> {

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
}
