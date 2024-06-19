package org.batfish.minesweeper.collectors;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.HasRoute;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.minesweeper.aspath.RoutingPolicyCollector;
import org.batfish.minesweeper.utils.Tuple;

/**
 * Collect all prefix-list names in a {@link org.batfish.datamodel.routing_policy.RoutingPolicy}.
 */
@ParametersAreNonnullByDefault
public class PrefixListNameCollector extends RoutingPolicyCollector<String> {
  @Override
  public Set<String> visitHasRoute(HasRoute hasRoute, Tuple<Set<String>, Configuration> arg) {
    if (hasRoute.getExpr() instanceof NamedPrefixSet) {
      return ImmutableSet.of(((NamedPrefixSet) hasRoute.getExpr()).getName());
    }
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitMatchPrefixSet(
      MatchPrefixSet matchPrefixSet, Tuple<Set<String>, Configuration> arg) {
    if (matchPrefixSet.getPrefixSet() instanceof NamedPrefixSet) {
      return ImmutableSet.of(((NamedPrefixSet) matchPrefixSet.getPrefixSet()).getName());
    }
    return ImmutableSet.of();
  }
}
