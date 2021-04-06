package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class PeerAs implements Uint16RangeExpr {

  @Nonnull
  public static PeerAs instance() {
    return INSTANCE;
  }

  @Override
  public <T, U> T accept(Uint16RangeExprVisitor<T, U> visitor, U arg) {
    return visitor.visitPeerAs(this);
  }

  private static final PeerAs INSTANCE = new PeerAs();

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof PeerAs;
  }

  @Override
  public int hashCode() {
    return 0xd8e56dbe; // randomly generated
  }

  private PeerAs() {}
}
