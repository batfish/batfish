package org.batfish.representation.cisco;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A structure representing a space of standard communities via a range expression for the high 16
 * bits and a range expression for the low 16 bits.
 */
@ParametersAreNonnullByDefault
public final class XrCommunitySetHighLowRangeExprs implements XrCommunitySetElem {

  public static @Nonnull XrCommunitySetHighLowRangeExprs of(long value) {
    int highInt = (int) ((value & 0xFFFF0000L) >> 16);
    int lowInt = (int) (value & 0xFFFFL);
    return new XrCommunitySetHighLowRangeExprs(
        new XrLiteralUint16(highInt), new XrLiteralUint16(lowInt));
  }

  public XrCommunitySetHighLowRangeExprs(
      XrUint16RangeExpr highRangeExpr, XrUint16RangeExpr lowRangeExpr) {
    _highRangeExpr = highRangeExpr;
    _lowRangeExpr = lowRangeExpr;
  }

  @Override
  public <T, U> T accept(XrCommunitySetElemVisitor<T, U> visitor, U arg) {
    return visitor.visitCommunitySetHighLowRangeExprs(this, arg);
  }

  public @Nonnull XrUint16RangeExpr getHighRangeExpr() {
    return _highRangeExpr;
  }

  public @Nonnull XrUint16RangeExpr getLowRangeExpr() {
    return _lowRangeExpr;
  }

  private final XrUint16RangeExpr _highRangeExpr;
  private final XrUint16RangeExpr _lowRangeExpr;
}
