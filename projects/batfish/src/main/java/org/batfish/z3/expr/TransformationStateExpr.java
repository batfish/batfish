package org.batfish.z3.expr;

import java.util.Objects;
import org.batfish.z3.expr.visitors.ExprVisitor;
import org.batfish.z3.expr.visitors.GenericGeneralStateExprVisitor;
import org.batfish.z3.expr.visitors.VoidStateExprVisitor;
import org.batfish.z3.state.visitors.GeneralStateVisitor;
import org.batfish.z3.state.visitors.Parameterizer;
import org.batfish.z3.state.visitors.StateVisitor;

public abstract class TransformationStateExpr extends StateExpr {

  public abstract static class State extends StateExpr.State {
    @Override
    public void accept(GeneralStateVisitor visitor) {
      visitor.visitTransformationStateExpr(this);
    }

    @Override
    public abstract void accept(StateVisitor visitor);
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitTransformationStateExpr(this);
  }

  /**
   * All {@code GenericStateExprVisitor} subclasses should have same behavior for all subclasses of
   * {@code TransformationStateExpr}.
   */
  @Override
  public <R> R accept(GenericGeneralStateExprVisitor<R> visitor) {
    return visitor.visitTransformationStateExpr(this);
  }

  @Override
  public void accept(VoidStateExprVisitor visitor) {
    visitor.visitTransformationStateExpr(this);
  }

  @Override
  public boolean exprEquals(Expr e) {
    return Parameterizer.getParameters(this)
        .equals(Parameterizer.getParameters((TransformationStateExpr) e));
  }

  @Override
  public abstract State getState();

  @Override
  public int hashCode() {
    return Objects.hash(getClass(), Parameterizer.getParameters(this));
  }
}
