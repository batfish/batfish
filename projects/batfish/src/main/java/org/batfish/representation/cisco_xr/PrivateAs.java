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

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof PrivateAs;
  }

  @Override
  public int hashCode() {
    return 0x956498d7; // randomly generated
  }

  private static final PrivateAs INSTANCE = new PrivateAs();

  private PrivateAs() {}
}
