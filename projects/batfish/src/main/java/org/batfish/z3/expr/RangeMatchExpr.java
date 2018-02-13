package org.batfish.z3.expr;

import com.google.common.collect.ImmutableList;
import java.util.Objects;
import java.util.Set;
import org.batfish.datamodel.SubRange;
import org.batfish.z3.HeaderField;
import org.batfish.z3.expr.visitors.BooleanExprVisitor;
import org.batfish.z3.expr.visitors.ExprVisitor;

public class RangeMatchExpr extends BooleanExpr {

  public static BooleanExpr greaterThanOrEqualTo(HeaderField bv, long lb, int numBits) {
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

  public static BooleanExpr lessThanOrEqualTo(HeaderField bv, long lb, int numBits) {
    LitIntExpr upperBound = new LitIntExpr(lb, numBits);
    VarIntExpr var = new VarIntExpr(bv);
    EqExpr exactMatch = new EqExpr(var, upperBound);
    BooleanExpr ge = greaterThanOrEqualTo(bv, lb, numBits);
    NotExpr lessThan = new NotExpr(ge);
    return new OrExpr(ImmutableList.of(exactMatch, lessThan));
  }

  private static IntExpr newExtractExpr(HeaderField var, int varSize, int low, int high) {
    if (low == 0 && high == varSize - 1) {
      return new VarIntExpr(var);
    } else {
      return ExtractExpr.newExtractExpr(var, low, high);
    }
  }

  private final BooleanExpr _expr;

  public RangeMatchExpr(HeaderField var, int bits, Set<SubRange> range) {
    long max = (1L << bits) - 1;
    ImmutableList.Builder<BooleanExpr> matchSomeSubRangeBuilder = ImmutableList.builder();
    for (SubRange subRange : range) {
      long low = subRange.getStart();
      long high = subRange.getEnd();
      if (low == high) {
        EqExpr exactMatch = new EqExpr(new VarIntExpr(var), new LitIntExpr(low, bits));
        matchSomeSubRangeBuilder.add(exactMatch);
      } else {
        boolean doLE = (high < max);
        boolean doGE = (low > 0);
        ImmutableList.Builder<BooleanExpr> matchThisSubRangeBuilder = ImmutableList.builder();
        if (doGE) {
          matchThisSubRangeBuilder.add(greaterThanOrEqualTo(var, low, bits));
        }
        if (doLE) {
          matchThisSubRangeBuilder.add(lessThanOrEqualTo(var, high, bits));
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
  public void accept(BooleanExprVisitor visitor) {
    visitor.visitRangeMatchExpr(this);
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitRangeMatchExpr(this);
  }

  @Override
  public boolean exprEquals(Expr e) {
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
