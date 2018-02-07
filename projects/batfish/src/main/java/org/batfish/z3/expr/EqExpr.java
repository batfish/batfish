package org.batfish.z3.expr;

public class EqExpr extends BooleanExpr {

  private final IntExpr _lhs;

  private final IntExpr _rhs;

  public EqExpr(IntExpr lhs, IntExpr rhs) {
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
