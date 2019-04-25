package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

public class ExitsNetwork extends StateExpr {

  public static class State extends StateExpr.State {

    public static final State INSTANCE = new State();

    private State() {}

    @Override
    public void accept(StateVisitor visitor) {
      visitor.visitExitsNetwork(this);
    }
  }

  public static final ExitsNetwork INSTANCE = new ExitsNetwork();

  private ExitsNetwork() {}

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitExitsNetwork(this);
  }

  @Override
  public State getState() {
    return State.INSTANCE;
  }
}
