package org.batfish.minesweeper.collectors;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.minesweeper.aspath.RoutePolicyStatementMatchCollector;
import org.batfish.minesweeper.utils.Tuple;

/**
 * Collects the names of all policies *directly* called in a {@link Statement}. By this, we mean it
 * only returns the calls made by the original policy, excluding any other policy calls made by the
 * callees.
 */
public final class RoutePolicyStatementCallCollector
    extends RoutePolicyStatementMatchCollector<String> {

  public RoutePolicyStatementCallCollector() {
    super(new RoutePolicyBooleanExprCallCollector());
  }

  @Override
  public Set<String> visitCallStatement(
      CallStatement callStatement, Tuple<Set<String>, Configuration> arg) {
    return ImmutableSet.of(callStatement.getCalledPolicyName());
  }
}
