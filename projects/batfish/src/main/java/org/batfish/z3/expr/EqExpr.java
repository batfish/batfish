package org.batfish.z3.expr;

import org.batfish.common.BatfishException;
import org.batfish.z3.expr.visitors.BooleanExprVisitor;
import org.batfish.z3.expr.visitors.ExprVisitor;

public class EqExpr extends BooleanExpr {

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
  public void accept(BooleanExprVisitor visitor) {
    visitor.visitEqExpr(this);
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitEqExpr(this);
  }

  public IntExpr getLhs() {
    return _lhs;
  }

  public IntExpr getRhs() {
    return _rhs;
  }
}
