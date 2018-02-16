package org.batfish.z3.expr;

import java.util.Objects;
import org.batfish.z3.expr.visitors.BooleanExprVisitor;
import org.batfish.z3.expr.visitors.ExprVisitor;
import org.batfish.z3.state.visitors.Parameterizer;
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

  @Override
  public boolean exprEquals(Expr e) {
    return Parameterizer.getParameters(this).equals(Parameterizer.getParameters((StateExpr) e));
  }

  @Override
  public int hashCode() {
    return Objects.hash(getClass(), Parameterizer.getParameters(this));
  }
}
