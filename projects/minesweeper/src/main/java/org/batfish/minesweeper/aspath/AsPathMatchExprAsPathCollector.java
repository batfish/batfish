package org.batfish.minesweeper.aspath;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchAny;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchExpr;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchExprReference;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchExprVisitor;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchRegex;
import org.batfish.datamodel.routing_policy.as_path.AsSetsMatchingRanges;
import org.batfish.datamodel.routing_policy.as_path.HasAsPathLength;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.minesweeper.SymbolicAsPathRegex;
import org.batfish.minesweeper.bdd.AsPathMatchExprToRegexes;

/** Collect all AS-path regexes in a {@link AsPathMatchExpr}. */
@ParametersAreNonnullByDefault
public class AsPathMatchExprAsPathCollector
    implements AsPathMatchExprVisitor<Set<SymbolicAsPathRegex>, Configuration> {

  @Override
  public Set<SymbolicAsPathRegex> visitAsPathMatchAny(
      AsPathMatchAny asPathMatchAny, Configuration arg) {
    return asPathMatchAny.getDisjuncts().stream()
        .flatMap(expr -> expr.accept(this, arg).stream())
        .collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public Set<SymbolicAsPathRegex> visitAsPathMatchExprReference(
      AsPathMatchExprReference asPathMatchExprReference, Configuration arg) {
    return arg.getAsPathMatchExprs().get(asPathMatchExprReference.getName()).accept(this, arg);
  }

  @Override
  public Set<SymbolicAsPathRegex> visitAsPathMatchRegex(
      AsPathMatchRegex asPathMatchRegex, Configuration arg) {
    return ImmutableSet.of(new SymbolicAsPathRegex(asPathMatchRegex.getRegex()));
  }

  @Override
  public Set<SymbolicAsPathRegex> visitAsSetsMatchingRanges(
      AsSetsMatchingRanges asSetsMatchingRanges, Configuration arg) {
    return ImmutableSet.of(new SymbolicAsPathRegex(asSetsMatchingRanges));
  }

  @Override
  public Set<SymbolicAsPathRegex> visitHasAsPathLength(
      HasAsPathLength hasAsPathLength, Configuration arg) {
    IntComparison cmp = hasAsPathLength.getComparison();
    if (!(cmp.getExpr() instanceof LiteralInt)) {
      return ImmutableSet.of();
    }

    Set<SymbolicAsPathRegex> any = ImmutableSet.of(SymbolicAsPathRegex.ALL_AS_PATHS);
    Set<SymbolicAsPathRegex> none = ImmutableSet.of();
    int val = ((LiteralInt) cmp.getExpr()).getValue();

    // See AsPathMatchExprToRegexes#visitHasAsPathLength
    return switch (cmp.getComparator()) {
      case EQ -> ImmutableSet.of();
      case GE -> val <= 0 ? any : none;
      case GT -> val < 0 ? any : none;
      case LE -> val >= AsPathMatchExprToRegexes.ASSUMED_MAX_AS_PATH_LENGTH ? any : none;
      case LT -> val > AsPathMatchExprToRegexes.ASSUMED_MAX_AS_PATH_LENGTH ? any : none;
    };
  }
}
