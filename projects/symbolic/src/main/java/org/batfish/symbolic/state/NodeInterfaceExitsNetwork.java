package org.batfish.symbolic.state;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class NodeInterfaceExitsNetwork extends InterfaceStateExpr {
  public NodeInterfaceExitsNetwork(String hostname, String iface) {
    super(hostname, iface);
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitNodeInterfaceExitsNetwork(this);
  }
}
