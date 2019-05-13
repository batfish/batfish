package org.batfish.symbolic.state;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class NodeInterfaceNeighborUnreachable extends InterfaceStateExpr {
  public NodeInterfaceNeighborUnreachable(String hostname, String iface) {
    super(hostname, iface);
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitNodeInterfaceNeighborUnreachable(this);
  }
}
