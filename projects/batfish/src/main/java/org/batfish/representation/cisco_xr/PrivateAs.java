package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class PrivateAs implements Uint16RangeExpr {

  @Nonnull
  public static PrivateAs instance() {
    return INSTANCE;
  }

  @Override
  public <T, U> T accept(Uint16RangeExprVisitor<T, U> visitor, U arg) {
    return visitor.visitPrivateAs(this);
  }

  private static final PrivateAs INSTANCE = new PrivateAs();

  private PrivateAs() {}
}
