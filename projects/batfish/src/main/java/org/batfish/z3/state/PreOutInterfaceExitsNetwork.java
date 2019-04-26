package org.batfish.z3.state;

import java.util.Objects;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.StateExprVisitor;

/**
 * A {@link StateExpr} for flows being forwarded out an interface with the {@link
 * org.batfish.datamodel.FlowDisposition#EXITS_NETWORK} disposition, before the outgoing ACL(s) or
 * transformation are applied.
 */
public final class PreOutInterfaceExitsNetwork implements StateExpr {
  private final String _hostname;
  private final String _interface;

  public PreOutInterfaceExitsNetwork(String hostname, String iface) {
    _hostname = hostname;
    _interface = iface;
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitPreOutInterfaceExitsNetwork(this);
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
    if (!(o instanceof PreOutInterfaceExitsNetwork)) {
      return false;
    }
    PreOutInterfaceExitsNetwork that = (PreOutInterfaceExitsNetwork) o;
    return _hostname.equals(that._hostname) && _interface.equals(that._interface);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostname, _interface);
  }
}
