package org.batfish.minesweeper.bdd;

import com.google.common.collect.ImmutableSet;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.routing_policy.as_path.*;
import org.batfish.minesweeper.SymbolicAsPathRegex;

/**
 * Create a BDD from a {@link AsPathMatchExpr} expression, such that the models of the BDD represent
 * all and only AS-paths that match the expression. This BDD is used as part of symbolic route
 * analysis of the {@link MatchAsPath} expression.
 */
@ParametersAreNonnullByDefault
public class AsPathMatchExprToBDD
    implements AsPathMatchExprVisitor<BDD, CommunitySetMatchExprToBDD.Arg> {
  @Override
  public BDD visitAsPathMatchAny(
      AsPathMatchAny asPathMatchAny, CommunitySetMatchExprToBDD.Arg arg) {
    return arg.getBDDRoute()
        .getFactory()
        .orAll(
            asPathMatchAny.getDisjuncts().stream()
                .map(matchExpr -> matchExpr.accept(this, arg))
                .collect(ImmutableSet.toImmutableSet()));
  }

  @Override
  public BDD visitAsPathMatchExprReference(
      AsPathMatchExprReference asPathMatchExprReference, CommunitySetMatchExprToBDD.Arg arg) {
    return arg.getTransferBDD()
        .getConfiguration()
        .getAsPathMatchExprs()
        .get(asPathMatchExprReference.getName())
        .accept(this, arg);
  }

  @Override
  public BDD visitAsPathMatchRegex(
      AsPathMatchRegex asPathMatchRegex, CommunitySetMatchExprToBDD.Arg arg) {
    return arg.getTransferBDD()
        .asPathRegexesToBDD(
            ImmutableSet.of(new SymbolicAsPathRegex(asPathMatchRegex.getRegex())),
            arg.getBDDRoute());
  }

  @Override
  public BDD visitAsSetsMatchingRanges(
      AsSetsMatchingRanges asSetsMatchingRanges, CommunitySetMatchExprToBDD.Arg arg) {
    throw new UnsupportedOperationException("Currently not supporting matching on AS ranges");
  }

  @Override
  public BDD visitHasAsPathLength(
      HasAsPathLength hasAsPathLength, CommunitySetMatchExprToBDD.Arg arg) {
    throw new UnsupportedOperationException("Currently not supporting matching on AS path length");
  }
}
