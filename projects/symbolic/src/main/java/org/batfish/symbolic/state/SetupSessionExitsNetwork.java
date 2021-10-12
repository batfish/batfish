package org.batfish.symbolic.state;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents the point at which a session is setup for a flow that will exit the network via a
 * particular interface.
 */
@ParametersAreNonnullByDefault
public final class SetupSessionExitsNetwork extends InterfaceStateExpr {
  public SetupSessionExitsNetwork(String hostname, String iface) {
    super(hostname, iface);
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitSetupSessionExitsNetwork(this);
  }
}
