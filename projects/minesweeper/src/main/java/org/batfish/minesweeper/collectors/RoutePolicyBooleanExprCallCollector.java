package org.batfish.minesweeper.collectors;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.WithEnvironmentExpr;
import org.batfish.minesweeper.aspath.BooleanExprMatchCollector;
import org.batfish.minesweeper.utils.Tuple;

/**
 * Collects the names of all policies *directly* called in a {@link
 * org.batfish.datamodel.routing_policy.expr.BooleanExpr}. By this, we mean it only returns the
 * calls made by the original policy, excluding any other policy calls made by the callees.
 */
public final class RoutePolicyBooleanExprCallCollector extends BooleanExprMatchCollector<String> {

  @Override
  public Set<String> visitCallExpr(CallExpr callExpr, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of(callExpr.getCalledPolicyName());
  }

  @Override
  public Set<String> visitWithEnvironmentExpr(
      WithEnvironmentExpr withEnvironmentExpr, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.<String>builder()
        .addAll(withEnvironmentExpr.getExpr().accept(this, arg))
        .addAll(
            new RoutePolicyStatementCallCollector()
                .visitAll(withEnvironmentExpr.getPreStatements(), arg))
        .addAll(
            new RoutePolicyStatementCallCollector()
                .visitAll(withEnvironmentExpr.getPostStatements(), arg))
        .addAll(
            new RoutePolicyStatementCallCollector()
                .visitAll(withEnvironmentExpr.getPostTrueStatements(), arg))
        .build();
  }
}
