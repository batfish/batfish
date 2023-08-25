package org.batfish.minesweeper.collectors;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.WithEnvironmentExpr;
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

  @Override
  public Set<String> visitCallExpr(CallExpr callExpr, Tuple<Set<String>, Configuration> arg) {
    if (arg.getFirst().contains(callExpr.getCalledPolicyName())) {
      // If we have already visited this policy then don't visit again
      return ImmutableSet.of();
    }
    // Otherwise update the set of seen policies and recurse.
    arg.getFirst().add(callExpr.getCalledPolicyName());

    return new RoutingPolicyStatementCommunityCollector()
        .visitAll(
            arg.getSecond()
                .getRoutingPolicies()
                .get(callExpr.getCalledPolicyName())
                .getStatements(),
            arg);
  }

  @Override
  public Set<String> visitWithEnvironmentExpr(
      WithEnvironmentExpr withEnvironmentExpr, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.<String>builder()
        .addAll(withEnvironmentExpr.getExpr().accept(this, arg))
        .addAll(
            new RoutingPolicyStatementCommunityCollector()
                .visitAll(withEnvironmentExpr.getPreStatements(), arg))
        .addAll(
            new RoutingPolicyStatementCommunityCollector()
                .visitAll(withEnvironmentExpr.getPostStatements(), arg))
        .addAll(
            new RoutingPolicyStatementCommunityCollector()
                .visitAll(withEnvironmentExpr.getPostTrueStatements(), arg))
        .build();
  }
}
