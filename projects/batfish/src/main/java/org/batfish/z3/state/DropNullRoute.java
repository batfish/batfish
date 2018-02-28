package org.batfish.z3.state;

import org.batfish.z3.expr.BasicStateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

public class DropNullRoute extends BasicStateExpr {

  public static class State extends BasicStateExpr.State {

    public static final State INSTANCE = new State();

    private State() {}

    @Override
    public void accept(StateVisitor visitor) {
      visitor.visitDropNullRoute(this);
    }
  }

  public static final DropNullRoute INSTANCE = new DropNullRoute();

  private DropNullRoute() {}

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitDropNullRoute(this);
  }

  @Override
  public State getState() {
    return State.INSTANCE;
  }
}
