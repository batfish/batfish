package org.batfish.minesweeper.collectors;

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

/** Collect all AS-path list names in a {@link AsPathMatchExpr}. */
@ParametersAreNonnullByDefault
public class AsPathNameAsPathMatchExprCollector
    implements AsPathMatchExprVisitor<Set<String>, Configuration> {

  @Override
  public Set<String> visitAsPathMatchAny(AsPathMatchAny asPathMatchAny, Configuration arg) {
    return asPathMatchAny.getDisjuncts().stream()
        .flatMap(expr -> expr.accept(this, arg).stream())
        .collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public Set<String> visitAsPathMatchExprReference(
      AsPathMatchExprReference asPathMatchExprReference, Configuration arg) {
    return ImmutableSet.of(asPathMatchExprReference.getName());
  }

  @Override
  public Set<String> visitAsPathMatchRegex(AsPathMatchRegex asPathMatchRegex, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitAsSetsMatchingRanges(
      AsSetsMatchingRanges asSetsMatchingRanges, Configuration arg) {
    return ImmutableSet.of();
  }

  @Override
  public Set<String> visitHasAsPathLength(HasAsPathLength hasAsPathLength, Configuration arg) {
    return ImmutableSet.of();
  }
}
