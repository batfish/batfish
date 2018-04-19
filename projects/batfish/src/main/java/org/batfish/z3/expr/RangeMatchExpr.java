package org.batfish.z3.expr;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import java.util.Objects;
import java.util.Set;
import org.batfish.datamodel.SubRange;
import org.batfish.z3.Field;
import org.batfish.z3.expr.visitors.ExprVisitor;
import org.batfish.z3.expr.visitors.GenericBooleanExprVisitor;

public final class RangeMatchExpr extends BooleanExpr {

  public static RangeMatchExpr fromSubRanges(Field field, int bits, Set<SubRange> range) {
    return fromSubRanges(new VarIntExpr(field), bits, range);
  }

  public static RangeMatchExpr fromSubRanges(VarIntExpr var, int bits, Set<SubRange> range) {
    return new RangeMatchExpr(
        var,
        bits,
        range
            .stream()
            .map(subRange -> Range.closed((long) subRange.getStart(), (long) subRange.getEnd()))
            .collect(ImmutableSet.toImmutableSet()));
  }

  public static BooleanExpr greaterThanOrEqualTo(Field field, long lb, int numBits) {
    return greaterThanOrEqualTo(new VarIntExpr(field), lb, numBits);
  }

  public static BooleanExpr greaterThanOrEqualTo(IntExpr bv, long lb, int numBits) {
    long remainingNumber = lb;
    int offset = 0;
    BooleanExpr currentExpr = TrueExpr.INSTANCE;
    boolean oddIteration = false;
    while (remainingNumber != 0L) {
      oddIteration = !oddIteration;
      if (oddIteration) {
        int numTrailingOnes = Long.numberOfTrailingZeros(~remainingNumber);
        int highestRemainingBitToUse = numTrailingOnes - 1;
        if (highestRemainingBitToUse < 0) {
          continue;
        }
        int bvLowBit = offset;
        int bvHighBit = bvLowBit + highestRemainingBitToUse;
        LitIntExpr remainingBitsToMatch =
            new LitIntExpr(remainingNumber, 0, highestRemainingBitToUse);
        IntExpr extractExpr = newExtractExpr(bv, numBits, bvLowBit, bvHighBit);
        EqExpr eqExpr = new EqExpr(extractExpr, remainingBitsToMatch);
        currentExpr = new AndExpr(ImmutableList.of(currentExpr, eqExpr));
        remainingNumber >>= numTrailingOnes;
        offset += numTrailingOnes;
      } else {
        int numTrailingZeros = Long.numberOfTrailingZeros(remainingNumber);
        int highestRemainingBitToUse = numTrailingZeros - 1;
        if (highestRemainingBitToUse < 0) {
          continue;
        }
        int bvLowBit = offset;
        int bvHighBit = bvLowBit + highestRemainingBitToUse;
        LitIntExpr remainingBitsToMatch = new LitIntExpr(0L, 0, highestRemainingBitToUse);
        IntExpr extractExpr = newExtractExpr(bv, numBits, bvLowBit, bvHighBit);
        EqExpr eqExpr = new EqExpr(extractExpr, remainingBitsToMatch);
        NotExpr notExpr = new NotExpr(eqExpr);
        currentExpr = new OrExpr(ImmutableList.of(currentExpr, notExpr));
        remainingNumber >>= numTrailingZeros;
        offset += numTrailingZeros;
      }
    }
    return currentExpr;
  }

  public static BooleanExpr lessThanOrEqualTo(IntExpr var, long lb, int numBits) {
    LitIntExpr upperBound = new LitIntExpr(lb, numBits);
    EqExpr exactMatch = new EqExpr(var, upperBound);
    BooleanExpr ge = greaterThanOrEqualTo(var, lb, numBits);
    NotExpr lessThan = new NotExpr(ge);
    return new OrExpr(ImmutableList.of(exactMatch, lessThan));
  }

  private static IntExpr newExtractExpr(IntExpr var, int varSize, int low, int high) {
    if (low == 0 && high == varSize - 1) {
      return var;
    } else {
      return ExtractExpr.newExtractExpr(var, low, high);
    }
  }

  private final BooleanExpr _expr;

  public RangeMatchExpr(IntExpr intExpr, int bits, Set<Range<Long>> range) {
    long max = (1L << bits) - 1;
    ImmutableList.Builder<BooleanExpr> matchSomeSubRangeBuilder = ImmutableList.builder();
    for (Range<Long> subRange : range) {
      long low = subRange.lowerEndpoint();
      long high = subRange.upperEndpoint();
      if (low == high) {
        EqExpr exactMatch = new EqExpr(intExpr, new LitIntExpr(low, bits));
        matchSomeSubRangeBuilder.add(exactMatch);
      } else {
        boolean doLE = (high < max);
        boolean doGE = (low > 0);
        ImmutableList.Builder<BooleanExpr> matchThisSubRangeBuilder = ImmutableList.builder();
        if (doGE) {
          matchThisSubRangeBuilder.add(greaterThanOrEqualTo(intExpr, low, bits));
        }
        if (doLE) {
          matchThisSubRangeBuilder.add(lessThanOrEqualTo(intExpr, high, bits));
        }
        if (!doGE && !doLE) {
          // any value in range matches
          _expr = TrueExpr.INSTANCE;
          return;
        }
        AndExpr matchThisSubRange = new AndExpr(matchThisSubRangeBuilder.build());
        matchSomeSubRangeBuilder.add(matchThisSubRange);
      }
    }
    OrExpr or = new OrExpr(matchSomeSubRangeBuilder.build());
    _expr = or;
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitRangeMatchExpr(this);
  }

  @Override
  public <R> R accept(GenericBooleanExprVisitor<R> visitor) {
    return visitor.visitRangeMatchExpr(this);
  }

  @Override
  protected boolean exprEquals(Expr e) {
    return Objects.equals(_expr, ((RangeMatchExpr) e)._expr);
  }

  public BooleanExpr getExpr() {
    return _expr;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_expr);
  }
}
