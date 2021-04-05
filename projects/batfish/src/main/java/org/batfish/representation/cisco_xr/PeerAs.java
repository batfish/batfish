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

  private PeerAs() {}
}
