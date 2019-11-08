package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** A named reference to a range of 16-bit unsigned integers via a named variable. */
@ParametersAreNonnullByDefault
public class Uint16Reference implements Uint16RangeExpr {

  public Uint16Reference(String var) {
    _var = var;
  }

  @Override
  public <T, U> T accept(Uint16RangeExprVisitor<T, U> visitor, U arg) {
    return visitor.visitUint16Reference(this, arg);
  }

  public @Nonnull String getVar() {
    return _var;
  }

  private final @Nonnull String _var;
}
