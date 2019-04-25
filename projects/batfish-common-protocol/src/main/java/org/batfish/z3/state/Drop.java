package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

public class Drop extends StateExpr {

  public static class State extends StateExpr.State {

    public static final State INSTANCE = new State();

    private State() {}

    @Override
    public void accept(StateVisitor visitor) {
      visitor.visitDrop(this);
    }
  }

  public static final Drop INSTANCE = new Drop();

  private Drop() {}

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitDrop(this);
  }

  @Override
  public State getState() {
    return State.INSTANCE;
  }
}
