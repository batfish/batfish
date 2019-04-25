package org.batfish.z3.expr;

import java.util.Objects;
import org.batfish.common.BatfishException;
import org.batfish.z3.expr.visitors.ExprVisitor;
import org.batfish.z3.expr.visitors.GenericBooleanExprVisitor;

public final class EqExpr extends BooleanExpr {

  private final IntExpr _lhs;

  private final IntExpr _rhs;

  public EqExpr(IntExpr lhs, IntExpr rhs) {
    int leftBits = lhs.numBits();
    int rightBits = rhs.numBits();
    if (leftBits != rightBits) {
      throw new BatfishException(
          String.format(
              "Width of left expression (%d) does not match with of right expression (%d)",
              leftBits, rightBits));
    }
    _lhs = lhs;
    _rhs = rhs;
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitEqExpr(this);
  }

  @Override
  public <R> R accept(GenericBooleanExprVisitor<R> visitor) {
    return visitor.visitEqExpr(this);
  }

  @Override
  protected boolean exprEquals(Expr e) {
    EqExpr other = (EqExpr) e;
    return Objects.equals(_lhs, other._lhs) && Objects.equals(_rhs, other._rhs);
  }

  public IntExpr getLhs() {
    return _lhs;
  }

  public IntExpr getRhs() {
    return _rhs;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_lhs, _rhs);
  }
}
