package org.batfish.minesweeper.collectors;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchExpr;
import org.batfish.datamodel.routing_policy.as_path.MatchAsPath;
import org.batfish.datamodel.routing_policy.expr.AsPathSetExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.LegacyMatchAsPath;
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;
import org.batfish.minesweeper.aspath.RoutingPolicyCollector;
import org.batfish.minesweeper.utils.Tuple;

/** Collect all AS-path list names in a {@link BooleanExpr}. */
@ParametersAreNonnullByDefault
public class AsPathNameBooleanExprCollector extends RoutingPolicyCollector<String> {

  @Override
  public Set<String> visitMatchAsPath(
      MatchAsPath matchAsPath, Tuple<Set<String>, Configuration> arg) {
    AsPathMatchExpr matchExpr = matchAsPath.getAsPathMatchExpr();
    return matchExpr.accept(new AsPathNameAsPathMatchExprCollector(), arg.getSecond());
  }

  @Override
  public Set<String> visitMatchLegacyAsPath(
      LegacyMatchAsPath legacyMatchAsPath, Tuple<Set<String>, Configuration> arg) {
    AsPathSetExpr expr = legacyMatchAsPath.getExpr();
    if (expr instanceof NamedAsPathSet) {
      return ImmutableSet.of(((NamedAsPathSet) expr).getName());
    }
    return ImmutableSet.of();
  }
}
