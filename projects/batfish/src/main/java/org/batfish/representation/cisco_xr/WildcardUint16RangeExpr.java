package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** An expression representing any 16-bit unsigned number */
@ParametersAreNonnullByDefault
public final class WildcardUint16RangeExpr implements Uint16RangeExpr {

  @Nonnull
  public static WildcardUint16RangeExpr instance() {
    return INSTANCE;
  }

  private static final WildcardUint16RangeExpr INSTANCE = new WildcardUint16RangeExpr();

  private WildcardUint16RangeExpr() {}

  @Override
  public <T, U> T accept(Uint16RangeExprVisitor<T, U> visitor, U arg) {
    return visitor.visitWildcardUint16RangeExpr(this);
  }
}
