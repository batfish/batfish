package org.batfish.z3.expr;

import java.util.Objects;
import org.batfish.z3.expr.visitors.ExprVisitor;
import org.batfish.z3.expr.visitors.GenericBooleanExprVisitor;

public final class IfThenElse extends BooleanExpr {

  private final BooleanExpr _condition;

  private final BooleanExpr _then;

  private final BooleanExpr _else;

  public IfThenElse(BooleanExpr condition, BooleanExpr then, BooleanExpr els) {
    _condition = condition;
    _then = then;
    _else = els;
  }

  @Override
  public <R> R accept(GenericBooleanExprVisitor<R> visitor) {
    return visitor.visitIfThenElse(this);
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitIfThenElse(this);
  }

  @Override
  protected boolean exprEquals(Expr e) {
    IfThenElse other = (IfThenElse) e;
    return Objects.equals(_condition, other._condition)
        && Objects.equals(_then, other._then)
        && Objects.equals(_else, other._else);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_condition, _then, _else);
  }

  public BooleanExpr getCondition() {
    return _condition;
  }

  public BooleanExpr getThen() {
    return _then;
  }

  public BooleanExpr getElse() {
    return _else;
  }
}
