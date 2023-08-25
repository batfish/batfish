package org.batfish.minesweeper.collectors;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.minesweeper.aspath.BooleanExprMatchCollector;
import org.batfish.minesweeper.utils.Tuple;

public class RoutePolicyBooleanExprCallCollector extends BooleanExprMatchCollector<String> {

  @Override
  public Set<String> visitCallExpr(CallExpr callExpr, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of(callExpr.getCalledPolicyName());
  }
}
