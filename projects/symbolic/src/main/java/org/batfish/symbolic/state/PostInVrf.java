package org.batfish.symbolic.state;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class PostInVrf extends VrfStateExpr {
  public PostInVrf(String hostname, String vrf) {
    super(hostname, vrf);
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitPostInVrf(this);
  }
}
