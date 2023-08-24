package org.batfish.minesweeper.aspath;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.AsPathAccessListLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchExpr;
import org.batfish.datamodel.routing_policy.as_path.MatchAsPath;
import org.batfish.datamodel.routing_policy.expr.AsPathSetExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.LegacyMatchAsPath;
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;
import org.batfish.minesweeper.SymbolicAsPathRegex;
import org.batfish.minesweeper.utils.Tuple;

/** Collect all AS-path regexes in a {@link BooleanExpr}. */
@ParametersAreNonnullByDefault
public class BooleanExprAsPathCollector extends BooleanExprMatchCollector<SymbolicAsPathRegex> {

  @Override
  public Set<SymbolicAsPathRegex> visitMatchAsPath(
      MatchAsPath matchAsPath, Tuple<Set<String>, Configuration> arg) {
    AsPathMatchExpr matchExpr = matchAsPath.getAsPathMatchExpr();
    return matchExpr.accept(new AsPathMatchExprAsPathCollector(), arg.getSecond());
  }

  @Override
  public Set<SymbolicAsPathRegex> visitMatchLegacyAsPath(
      LegacyMatchAsPath legacyMatchAsPath, Tuple<Set<String>, Configuration> arg) {
    AsPathSetExpr expr = legacyMatchAsPath.getExpr();
    if (expr instanceof NamedAsPathSet) {
      NamedAsPathSet namedSet = (NamedAsPathSet) expr;
      AsPathAccessList list = arg.getSecond().getAsPathAccessLists().get(namedSet.getName());
      // conversion to VI should guarantee list is not null
      assert list != null;
      return list.getLines().stream()
          .map(AsPathAccessListLine::getRegex)
          .map(SymbolicAsPathRegex::new)
          .collect(ImmutableSet.toImmutableSet());
    } else {
      return ImmutableSet.of();
    }
  }

  @Override
  public Set<SymbolicAsPathRegex> visitDisjunction(
      Disjunction disjunction, Tuple<Set<String>, Configuration> arg) {
    Set<SymbolicAsPathRegex> disjuncts = visitAll(this, disjunction.getDisjuncts(), arg);
    if (disjunction.getDisjuncts().stream().allMatch(d -> d instanceof MatchAsPath)) {
      List<SymbolicAsPathRegex> disjuncts_list = new ArrayList<>(disjuncts);
      if (disjuncts_list.isEmpty()) {
        return ImmutableSet.of();
      }

      SymbolicAsPathRegex acc = disjuncts_list.get(0);
      for (int i = 1; i < disjuncts_list.size(); i++) {
        acc = acc.union(disjuncts_list.get(i));
      }
      return ImmutableSet.of(acc);
    } else {
      return disjuncts;
    }
  }
}
