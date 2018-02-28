package org.batfish.z3.state;

import org.batfish.z3.expr.BasicStateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

public class NodeTransit extends BasicStateExpr {

  public static class State extends BasicStateExpr.State {

    public static final State INSTANCE = new State();

    private State() {}

    @Override
    public void accept(StateVisitor visitor) {
      visitor.visitNodeTransit(this);
    }
  }

  private final String _hostname;

  public NodeTransit(String hostname) {
    _hostname = hostname;
  }

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitNodeTransit(this);
  }

  public String getHostname() {
    return _hostname;
  }

  @Override
  public State getState() {
    return State.INSTANCE;
  }
}
