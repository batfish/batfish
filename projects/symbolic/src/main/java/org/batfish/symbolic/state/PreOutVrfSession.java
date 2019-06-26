package org.batfish.symbolic.state;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class PreOutVrfSession extends VrfStateExpr {
  public PreOutVrfSession(String hostname, String vrf) {
    super(hostname, vrf);
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitPreOutVrfSession(this);
  }
}
