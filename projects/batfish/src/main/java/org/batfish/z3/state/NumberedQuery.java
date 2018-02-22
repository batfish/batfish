package org.batfish.z3.state;

import org.batfish.z3.expr.BasicStateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

public class NumberedQuery extends BasicStateExpr {

  public static class State extends BasicStateExpr.State {

    public static final State INSTANCE = new State();

    private State() {}

    @Override
    public void accept(StateVisitor visitor) {
      visitor.visitNumberedQuery(this);
    }
  }

  private final int _line;

  public NumberedQuery(int line) {
    _line = line;
  }

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitNumberedQuery(this);
  }

  public int getLine() {
    return _line;
  }

  @Override
  public State getState() {
    return State.INSTANCE;
  }
}
