package org.batfish.symbolic.state;

import javax.annotation.ParametersAreNonnullByDefault;

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
