package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.StateExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

public class Debug extends StateExpr {

  public static class State extends StateExpr.State {

    public static final State INSTANCE = new State();

    private State() {}

    @Override
    public void accept(StateVisitor visitor) {
      visitor.visitDebug(this);
    }
  }

  public static final Debug INSTANCE = new Debug();

  private Debug() {}

  @Override
  public void accept(StateExprVisitor visitor) {
    visitor.visitDebug(this);
  }
}
