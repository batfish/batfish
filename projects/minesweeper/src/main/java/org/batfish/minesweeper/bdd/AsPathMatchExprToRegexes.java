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
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
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

  /**
   * Reasonable cap on AS Path length in a non-attack scenario. Chosen semi-arbitrarily and used to
   * model {@link HasAsPathLength} which is likely only used to reject attacker-looking (or other
   * resource limitations) advertisements.
   */
  public static int ASSUMED_MAX_AS_PATH_LENGTH = 64;

  @Override
  public Set<SymbolicAsPathRegex> visitHasAsPathLength(
      HasAsPathLength hasAsPathLength, CommunitySetMatchExprToBDD.Arg arg) {
    IntComparison cmp = hasAsPathLength.getComparison();
    if (!(cmp.getExpr() instanceof LiteralInt)) {
      throw new UnsupportedOperationException(hasAsPathLength.toString());
    }

    // Similar to CommunitySet.HasSize, this construct is mainly useful for checking that the AS
    // path isn't so long it looks like an attack. Pick a semi-arbitrary threshold (64 ASNs) and
    // assume every realistic AS Path is shorter than that.
    // Return true if the filter allows all paths length 64 or less, and false otherwise.
    int val = ((LiteralInt) cmp.getExpr()).getValue();
    Set<SymbolicAsPathRegex> any = ImmutableSet.of(SymbolicAsPathRegex.ALL_AS_PATHS);
    Set<SymbolicAsPathRegex> none = ImmutableSet.of();
    return switch (cmp.getComparator()) {
      case EQ -> throw new UnsupportedOperationException(hasAsPathLength.toString());
      case GE -> val <= 0 ? any : none;
      case GT -> val < 0 ? any : none;
      case LE -> val >= ASSUMED_MAX_AS_PATH_LENGTH ? any : none;
      case LT -> val > ASSUMED_MAX_AS_PATH_LENGTH ? any : none;
    };
  }
}
