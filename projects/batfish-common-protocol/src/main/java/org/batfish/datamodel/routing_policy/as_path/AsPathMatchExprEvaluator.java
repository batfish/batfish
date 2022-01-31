package org.batfish.datamodel.routing_policy.as_path;

import com.google.common.collect.Range;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.common.util.PatternProvider;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.routing_policy.expr.IntMatchExprEvaluator;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;

/** A visitor for evaluating an {@link AsPathMatchExpr} under an {@link AsPathContext}. */
public final class AsPathMatchExprEvaluator implements AsPathMatchExprVisitor<Boolean, AsPath> {

  public AsPathMatchExprEvaluator(AsPathContext ctx) {
    _ctx = ctx;
  }

  private final @Nonnull AsPathContext _ctx;

  @Override
  public Boolean visitAsPathMatchAny(AsPathMatchAny asPathMatchAny, AsPath arg) {
    return asPathMatchAny.getDisjuncts().stream().anyMatch(disjunct -> disjunct.accept(this, arg));
  }

  @Override
  public Boolean visitAsPathMatchExprReference(
      AsPathMatchExprReference asPathMatchExprReference, AsPath arg) {
    AsPathMatchExpr expr = _ctx.getAsPathMatchExprs().get(asPathMatchExprReference.getName());
    // conversion to VI should guarantee expr is not null
    assert expr != null;
    return expr.accept(this, arg);
  }

  @Override
  public Boolean visitAsPathMatchRegex(AsPathMatchRegex asPathMatchRegex, AsPath arg) {
    // TODO: optimize
    return PatternProvider.fromString(asPathMatchRegex.getRegex())
        .matcher(arg.getAsPathString())
        .find();
  }

  @Override
  public Boolean visitAsSetsMatchingRanges(AsSetsMatchingRanges asSetsMatchingRanges, AsPath arg) {
    List<Range<Long>> asRanges = asSetsMatchingRanges.getAsRanges();
    List<AsSet> asSets = arg.getAsSets();
    int numRanges = asRanges.size();
    int numAsSets = arg.length();
    if (numAsSets < numRanges) {
      return false;
    }
    boolean anchorStart = asSetsMatchingRanges.getAnchorStart();
    boolean anchorEnd = asSetsMatchingRanges.getAnchorEnd();
    if (anchorStart) {
      if (anchorEnd && numAsSets != numRanges) {
        return false;
      }
      return tryMatchRanges(asRanges, asSets.subList(0, numRanges));
    } else if (anchorEnd) {
      return tryMatchRanges(asRanges, asSets.subList(numAsSets - numRanges, numAsSets));
    } else {
      // no anchor
      for (int i = 0; i <= numAsSets - numRanges; i++) {
        if (tryMatchRanges(asRanges, asSets.subList(i, i + numRanges))) {
          return true;
        }
      }
      return false;
    }
  }

  private boolean tryMatchRanges(List<Range<Long>> asRanges, List<AsSet> asSets) {
    assert asRanges.size() == asSets.size();
    for (int i = 0; i < asRanges.size(); i++) {
      Range<Long> currentRange = asRanges.get(i);
      if (asSets.get(i).getAsns().stream().noneMatch(currentRange::contains)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Boolean visitHasAsPathLength(HasAsPathLength hasAsPathLength, AsPath arg) {
    return hasAsPathLength
        .getComparison()
        .accept(IntMatchExprEvaluator.instance(), new LiteralInt(arg.length()));
  }
}
