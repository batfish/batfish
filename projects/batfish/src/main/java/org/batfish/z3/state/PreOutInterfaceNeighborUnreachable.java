package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.StateExprVisitor;

import java.util.Objects;

/**
 * A {@link StateExpr} for flows being forwarded out an interface with the {@link
 * org.batfish.datamodel.FlowDisposition#NEIGHBOR_UNREACHABLE} disposition, before the outgoing
 * ACL(s) or transformation are applied.
 */
public final class PreOutInterfaceNeighborUnreachable implements StateExpr {
  private final String _hostname;
  private final String _interface;

  public PreOutInterfaceNeighborUnreachable(String hostname, String iface) {
    _hostname = hostname;
    _interface = iface;
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitPreOutInterfaceNeighborUnreachable(this);
  }

  public String getHostname() {
    return _hostname;
  }

  public String getInterface() {
    return _interface;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PreOutInterfaceNeighborUnreachable)) {
      return false;
    }

    PreOutInterfaceNeighborUnreachable that = (PreOutInterfaceNeighborUnreachable) o;
    return Objects.equals(_hostname, that._hostname) && Objects.equals(_interface, that._interface);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostname, _interface);
  }
}
