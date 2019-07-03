package org.batfish.symbolic.state;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class PostInVrfSession extends VrfStateExpr {
  public PostInVrfSession(String hostname, String vrf) {
    super(hostname, vrf);
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitPostInVrfSession(this);
  }
}
