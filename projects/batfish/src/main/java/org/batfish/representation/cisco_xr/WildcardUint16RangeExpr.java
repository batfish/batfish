package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** An expression representing any 16-bit unsigned number */
@ParametersAreNonnullByDefault
public final class WildcardUint16RangeExpr implements Uint16RangeExpr {

  public static @Nonnull WildcardUint16RangeExpr instance() {
    return INSTANCE;
  }

  private static final WildcardUint16RangeExpr INSTANCE = new WildcardUint16RangeExpr();

  private WildcardUint16RangeExpr() {}

  @Override
  public <T, U> T accept(Uint16RangeExprVisitor<T, U> visitor, U arg) {
    return visitor.visitWildcardUint16RangeExpr(this);
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof WildcardUint16RangeExpr;
  }

  @Override
  public int hashCode() {
    return 0x0fc88b43; // randomly generated
  }
}
