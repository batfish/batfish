package org.batfish.symbolic.state;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class OriginateVrf extends VrfStateExpr {
  public OriginateVrf(String hostname, String vrf) {
    super(hostname, vrf);
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitOriginateVrf(this);
  }
}
