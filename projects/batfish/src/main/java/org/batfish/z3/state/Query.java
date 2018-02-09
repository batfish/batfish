package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.StateExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

public class Query extends StateExpr {

  public static class State extends StateExpr.State {

    public static final State INSTANCE = new State();

    private State() {}

    @Override
    public void accept(StateVisitor visitor) {
      visitor.visitQuery(this);
    }
  }

  public static final Query INSTANCE = new Query();

  private Query() {}

  @Override
  public void accept(StateExprVisitor visitor) {
    visitor.visitQuery(this);
  }
}
