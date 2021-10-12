package org.batfish.symbolic.state;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents the point at which a session is setup for a flow that will be delivered to the subnet
 * connected to a particular interface.
 */
@ParametersAreNonnullByDefault
public final class SetupSessionDeliveredToSubnet extends InterfaceStateExpr {
  public SetupSessionDeliveredToSubnet(String hostname, String iface) {
    super(hostname, iface);
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitSetupSessionDeliveredToSubnet(this);
  }
}
