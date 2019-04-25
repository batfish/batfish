package org.batfish.z3.expr;

import com.google.common.base.Suppliers;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.batfish.z3.expr.visitors.ExprVisitor;
import org.batfish.z3.state.StateParameter;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;
import org.batfish.z3.state.visitors.Parameterizer;
import org.batfish.z3.state.visitors.StateVisitor;

/** An expression representing parameterized state. */
public abstract class StateExpr extends Expr {
  private final Supplier<Integer> _hashCode = Suppliers.memoize(this::computeHashCode);
  private final Supplier<List<StateParameter>> _params = Suppliers.memoize(this::computeParameters);

  public abstract static class State {
    public abstract void accept(StateVisitor visitor);
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitStateExpr(this);
  }

  public abstract <R> R accept(GenericStateExprVisitor<R> visitor);

  private int computeHashCode() {
    return Objects.hash(getClass().getCanonicalName(), _params.get());
  }

  private List<StateParameter> computeParameters() {
    return Parameterizer.getParameters(this);
  }

  @Override
  protected final boolean exprEquals(Expr e) {
    return _params.get().equals(((StateExpr) e)._params.get());
  }

  public abstract State getState();

  @Override
  public int hashCode() {
    return _hashCode.get();
  }
}
