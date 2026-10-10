package org.batfish.minesweeper.aspath;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.RoutingPolicyTraverser;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.minesweeper.utils.Tuple;

/**
 * Collect a set of items in a {@link RoutingPolicy}. This class automatically keeps track of
 * policies that have already been visited, to prevent cycles when traversing called policies.
 */
@ParametersAreNonnullByDefault
public class RoutingPolicyCollector<T>
    extends RoutingPolicyTraverser<Set<T>, Tuple<Set<String>, Configuration>> {

  @Override
  protected Set<T> defaultResult() {
    return ImmutableSet.of();
  }

  @Override
  protected Set<T> combineResults(Stream<Set<T>> results) {
    return results.flatMap(Set::stream).collect(ImmutableSet.toImmutableSet());
  }

  @Override
  protected Set<T> visitCalledPolicy(String policyName, Tuple<Set<String>, Configuration> arg) {
    if (!arg.getFirst().add(policyName)) {
      return ImmutableSet.of();
    }
    RoutingPolicy policy = arg.getSecond().getRoutingPolicies().get(policyName);
    return policy == null ? ImmutableSet.of() : visitStatements(policy.getStatements(), arg);
  }

  /** Traverses {@code statements} and returns the collected items. */
  public Set<T> visitAll(List<Statement> statements, Tuple<Set<String>, Configuration> arg) {
    return visitStatements(statements, arg);
  }

  /** Traverses {@code expressions} and returns the collected items. */
  public static <T> Set<T> visitAll(
      RoutingPolicyCollector<T> visitor,
      List<BooleanExpr> expressions,
      Tuple<Set<String>, Configuration> arg) {
    return visitor.visitBooleanExpressions(expressions, arg);
  }
}
