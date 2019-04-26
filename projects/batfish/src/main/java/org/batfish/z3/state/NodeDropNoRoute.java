package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;

public class NodeDropNoRoute extends StateExpr {

  private final String _hostname;

  public NodeDropNoRoute(String hostname) {
    _hostname = hostname;
  }

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitNodeDropNoRoute(this);
  }

  public String getHostname() {
    return _hostname;
  }
}
