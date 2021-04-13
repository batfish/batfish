package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** An expression representing any 32-bit unsigned number */
@ParametersAreNonnullByDefault
public final class WildcardUint32RangeExpr implements Uint32RangeExpr {

  @Nonnull
  public static WildcardUint32RangeExpr instance() {
    return INSTANCE;
  }

  private static final WildcardUint32RangeExpr INSTANCE = new WildcardUint32RangeExpr();

  private WildcardUint32RangeExpr() {}

  @Override
  public <T, U> T accept(Uint32RangeExprVisitor<T, U> visitor, U arg) {
    return visitor.visitWildcardUint32RangeExpr(this);
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof WildcardUint32RangeExpr;
  }

  @Override
  public int hashCode() {
    return 0x9a5b0921; // randomly generated
  }
}
