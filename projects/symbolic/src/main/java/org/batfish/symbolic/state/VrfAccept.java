package org.batfish.symbolic.state;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class VrfAccept extends VrfStateExpr {
  public VrfAccept(String hostname, String vrf) {
    super(hostname, vrf);
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitVrfAccept(this);
  }
}
