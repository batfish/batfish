package org.batfish.z3.state;

import org.batfish.z3.expr.BasicStateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

public class NeighborUnreachable extends BasicStateExpr {

  public static class State extends BasicStateExpr.State {

    public static final State INSTANCE = new State();

    private State() {}

    @Override
    public void accept(StateVisitor visitor) {
      visitor.visitNeighborUnreachable(this);
    }
  }

  public static final NeighborUnreachable INSTANCE = new NeighborUnreachable();

  private NeighborUnreachable() {}

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitNeighborUnreachable(this);
  }

  @Override
  public State getState() {
    return State.INSTANCE;
  }
}
