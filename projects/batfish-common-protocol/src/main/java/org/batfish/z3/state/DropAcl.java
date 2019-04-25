package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

public class DropAcl extends StateExpr {

  public static class State extends StateExpr.State {

    public static final State INSTANCE = new State();

    private State() {}

    @Override
    public void accept(StateVisitor visitor) {
      visitor.visitDropAcl(this);
    }
  }

  public static final DropAcl INSTANCE = new DropAcl();

  private DropAcl() {}

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitDropAcl(this);
  }

  @Override
  public State getState() {
    return State.INSTANCE;
  }
}
