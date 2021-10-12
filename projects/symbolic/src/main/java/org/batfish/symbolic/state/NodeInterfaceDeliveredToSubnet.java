package org.batfish.symbolic.state;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents the point at which a flow has been delivered to the subnet connected to a particular
 * interface.
 */
@ParametersAreNonnullByDefault
public final class NodeInterfaceDeliveredToSubnet extends InterfaceStateExpr {
  public NodeInterfaceDeliveredToSubnet(String hostname, String iface) {
    super(hostname, iface);
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitNodeInterfaceDeliveredToSubnet(this);
  }
}
