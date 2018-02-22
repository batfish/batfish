package org.batfish.z3.expr;

import java.util.Objects;
import org.batfish.z3.expr.visitors.ExprVisitor;
import org.batfish.z3.expr.visitors.GenericBooleanExprVisitor;

/**
 * Represents a lazy transformation of the contained {@link BooleanExpr} to one where any
 * pre-transformation states are interpreted as post-transformation states.
 */
public class TransformedExpr extends BooleanExpr {

  private final BooleanExpr _subExpression;

  public TransformedExpr(BooleanExpr subExpression) {
    _subExpression = subExpression;
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitTransformedExpr(this);
  }

  @Override
  public <R> R accept(GenericBooleanExprVisitor<R> visitor) {
    return visitor.visitTransformedExpr(this);
  }

  @Override
  public boolean exprEquals(Expr e) {
    return Objects.equals(_subExpression, ((TransformedExpr) e)._subExpression);
  }

  public BooleanExpr getSubExpression() {
    return _subExpression;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_subExpression);
  }
}
