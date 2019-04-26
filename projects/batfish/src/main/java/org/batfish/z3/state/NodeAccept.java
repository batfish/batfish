package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.StateExprVisitor;

import java.util.Objects;

public final class NodeAccept implements StateExpr {

  private final String _hostname;

  public NodeAccept(String hostname) {
    _hostname = hostname;
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitNodeAccept(this);
  }

  public String getHostname() {
    return _hostname;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NodeAccept)) {
      return false;
    }
    NodeAccept that = (NodeAccept) o;
    return Objects.equals(_hostname, that._hostname);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostname);
  }
}
