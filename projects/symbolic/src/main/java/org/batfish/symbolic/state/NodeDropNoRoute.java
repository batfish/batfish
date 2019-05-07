package org.batfish.symbolic.state;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class NodeDropNoRoute implements StateExpr {

  private final String _hostname;

  public NodeDropNoRoute(String hostname) {
    _hostname = hostname;
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitNodeDropNoRoute(this);
  }

  public String getHostname() {
    return _hostname;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NodeDropNoRoute)) {
      return false;
    }
    NodeDropNoRoute that = (NodeDropNoRoute) o;
    return _hostname.equals(that._hostname);
  }

  @Override
  public int hashCode() {
    return _hostname.hashCode();
  }
}
