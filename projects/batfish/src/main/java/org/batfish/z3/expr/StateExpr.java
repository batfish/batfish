package org.batfish.z3.expr;

import java.util.List;
import java.util.Objects;
import org.batfish.z3.expr.visitors.ExprVisitor;
import org.batfish.z3.state.StateParameter;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;
import org.batfish.z3.state.visitors.Parameterizer;
import org.batfish.z3.state.visitors.StateVisitor;

/** An expression representing parameterized state. */
public abstract class StateExpr extends Expr {
  private Integer _hashCode;
  private List<StateParameter> _params;

  public abstract static class State {
    public abstract void accept(StateVisitor visitor);
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitStateExpr(this);
  }

  public abstract <R> R accept(GenericStateExprVisitor<R> visitor);

  private List<StateParameter> getParameters() {
    if (_params == null) {
      _params = Parameterizer.getParameters(this);
    }
    return _params;
  }

  @Override
  protected final boolean exprEquals(Expr e) {
    return getParameters().equals(((StateExpr) e).getParameters());
  }

  public abstract State getState();

  @Override
  public int hashCode() {
    if (_hashCode == null) {
      _hashCode = Objects.hash(getClass(), getParameters());
    }
    return _hashCode;
  }
}
