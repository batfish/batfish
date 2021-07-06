package org.batfish.minesweeper.aspath;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.as_path.*;
import org.batfish.minesweeper.SymbolicAsPathRegex;

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
    return ImmutableSet.of();
  }

  @Override
  public Set<SymbolicAsPathRegex> visitHasAsPathLength(
      HasAsPathLength hasAsPathLength, Configuration arg) {
    return ImmutableSet.of();
  }
}
