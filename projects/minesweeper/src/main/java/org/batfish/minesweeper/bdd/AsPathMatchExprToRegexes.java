package org.batfish.minesweeper.bdd;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchAny;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchExpr;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchExprReference;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchExprVisitor;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchRegex;
import org.batfish.datamodel.routing_policy.as_path.AsSetsMatchingRanges;
import org.batfish.datamodel.routing_policy.as_path.HasAsPathLength;
import org.batfish.datamodel.routing_policy.as_path.MatchAsPath;
import org.batfish.minesweeper.SymbolicAsPathRegex;

/**
 * Create a set of {@link SymbolicAsPathRegex} objects from a {@link AsPathMatchExpr} expression,
 * such that the disjunction of these regexes represent all and only AS-paths that match the
 * expression. This set is used as part of symbolic route analysis of the {@link MatchAsPath}
 * expression.
 */
@ParametersAreNonnullByDefault
public class AsPathMatchExprToRegexes
    implements AsPathMatchExprVisitor<Set<SymbolicAsPathRegex>, CommunitySetMatchExprToBDD.Arg> {
  @Override
  public Set<SymbolicAsPathRegex> visitAsPathMatchAny(
      AsPathMatchAny asPathMatchAny, CommunitySetMatchExprToBDD.Arg arg) {
    return asPathMatchAny.getDisjuncts().stream()
        .flatMap(matchExpr -> matchExpr.accept(this, arg).stream())
        .collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public Set<SymbolicAsPathRegex> visitAsPathMatchExprReference(
      AsPathMatchExprReference asPathMatchExprReference, CommunitySetMatchExprToBDD.Arg arg) {
    return arg.getAsPathMatchExpr(asPathMatchExprReference.getName()).accept(this, arg);
  }

  @Override
  public Set<SymbolicAsPathRegex> visitAsPathMatchRegex(
      AsPathMatchRegex asPathMatchRegex, CommunitySetMatchExprToBDD.Arg arg) {
    return ImmutableSet.of(new SymbolicAsPathRegex(asPathMatchRegex.getRegex()));
  }

  @Override
  public Set<SymbolicAsPathRegex> visitAsSetsMatchingRanges(
      AsSetsMatchingRanges asSetsMatchingRanges, CommunitySetMatchExprToBDD.Arg arg) {
    return ImmutableSet.of(new SymbolicAsPathRegex(asSetsMatchingRanges));
  }

  @Override
  public Set<SymbolicAsPathRegex> visitHasAsPathLength(
      HasAsPathLength hasAsPathLength, CommunitySetMatchExprToBDD.Arg arg) {
    throw new UnsupportedOperationException(hasAsPathLength.toString());
  }
}
