package org.batfish.z3.expr;

import java.util.Objects;
import org.batfish.z3.expr.visitors.GenericGeneralStateExprVisitor;
import org.batfish.z3.expr.visitors.VoidStateExprVisitor;
import org.batfish.z3.state.visitors.GeneralStateVisitor;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;
import org.batfish.z3.state.visitors.Parameterizer;
import org.batfish.z3.state.visitors.StateVisitor;

/** An expression representing parameterized state. */
public abstract class StateExpr extends Expr {

  public abstract static class State {
    public abstract void accept(GeneralStateVisitor visitor);

    public abstract void accept(StateVisitor visitor);
  }

  public abstract <R> R accept(GenericGeneralStateExprVisitor<R> visitor);

  public abstract <R> R accept(GenericStateExprVisitor<R> visitor);

  public abstract void accept(VoidStateExprVisitor visitor);

  @Override
  public boolean exprEquals(Expr e) {
    return Parameterizer.getParameters(this).equals(Parameterizer.getParameters((StateExpr) e));
  }

  public abstract State getState();

  @Override
  public int hashCode() {
    return Objects.hash(getClass(), Parameterizer.getParameters(this));
  }
}
