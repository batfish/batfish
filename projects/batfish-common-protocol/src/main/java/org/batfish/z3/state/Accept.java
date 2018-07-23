package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

public class Accept extends StateExpr {

  public static class State extends StateExpr.State {

    public static final State INSTANCE = new State();

    private State() {}

    @Override
    public void accept(StateVisitor visitor) {
      visitor.visitAccept(this);
    }
  }

  public static final Accept INSTANCE = new Accept();

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitAccept(this);
  }

  @Override
  public State getState() {
    return State.INSTANCE;
  }
}
