package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

public class InsufficientInfo extends StateExpr {

  public static class State extends StateExpr.State {

    public static final State INSTANCE = new State();

    private State() {}

    @Override
    public void accept(StateVisitor visitor) {
      visitor.visitInsufficientInfo(this);
    }
  }

  public static final InsufficientInfo INSTANCE = new InsufficientInfo();

  private InsufficientInfo() {}

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitInsufficientInfo(this);
  }

  @Override
  public State getState() {
    return State.INSTANCE;
  }
}
