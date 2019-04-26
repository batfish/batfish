package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;

/**
 * A {@link StateExpr} for flows being forwarded out an interface with the {@link
 * org.batfish.datamodel.FlowDisposition#NEIGHBOR_UNREACHABLE} disposition, before the outgoing
 * ACL(s) or transformation are applied.
 */
public final class PreOutInterfaceNeighborUnreachable extends StateExpr {
  private final String _hostname;
  private final String _interface;

  public PreOutInterfaceNeighborUnreachable(String hostname, String iface) {
    _hostname = hostname;
    _interface = iface;
  }

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitPreOutInterfaceNeighborUnreachable(this);
  }

  public String getHostname() {
    return _hostname;
  }

  public String getInterface() {
    return _interface;
  }
}
