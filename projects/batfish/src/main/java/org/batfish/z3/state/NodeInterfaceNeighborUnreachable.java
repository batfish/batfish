package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;

public class NodeInterfaceNeighborUnreachable extends StateExpr {

  private final String _hostname;

  private final String _iface;

  public NodeInterfaceNeighborUnreachable(String hostname, String iface) {
    _hostname = hostname;
    _iface = iface;
  }

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitNodeInterfaceNeighborUnreachable(this);
  }

  public String getHostname() {
    return _hostname;
  }

  public String getIface() {
    return _iface;
  }
}
