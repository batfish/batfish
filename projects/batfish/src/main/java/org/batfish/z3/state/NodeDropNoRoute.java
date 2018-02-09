package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.StateExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

public class NodeDropNoRoute extends StateExpr {

  public static class State extends StateExpr.State {

    public static final State INSTANCE = new State();

    private State() {}

    @Override
    public void accept(StateVisitor visitor) {
      visitor.visitNodeDropNoRoute(this);
    }
  }

  private final String _hostname;

  public NodeDropNoRoute(String hostname) {
    _hostname = hostname;
  }

  @Override
  public void accept(StateExprVisitor visitor) {
    visitor.visitNodeDropNoRoute(this);
  }

  public String getHostname() {
    return _hostname;
  }
}
