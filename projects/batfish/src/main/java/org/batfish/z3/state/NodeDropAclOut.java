package org.batfish.z3.state;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.StateExprVisitor;

@ParametersAreNonnullByDefault
public final class NodeDropAclOut implements StateExpr {

  private final String _hostname;

  public NodeDropAclOut(String hostname) {
    _hostname = hostname;
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitNodeDropAclOut(this);
  }

  public String getHostname() {
    return _hostname;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NodeDropAclOut)) {
      return false;
    }
    NodeDropAclOut that = (NodeDropAclOut) o;
    return _hostname.equals(that._hostname);
  }

  @Override
  public int hashCode() {
    return _hostname.hashCode();
  }
}
