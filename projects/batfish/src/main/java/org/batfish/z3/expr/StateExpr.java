package org.batfish.z3.expr;

import org.batfish.z3.expr.visitors.BooleanExprVisitor;
import org.batfish.z3.expr.visitors.ExprVisitor;
import org.batfish.z3.state.visitors.StateExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

public abstract class StateExpr extends BooleanExpr {

  public abstract static class State {
    public abstract void accept(StateVisitor visitor);
  }

  @Override
  public void accept(BooleanExprVisitor visitor) {
    visitor.visitStateExpr(this);
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitStateExpr(this);
  }

  public abstract void accept(StateExprVisitor visitor);
}
