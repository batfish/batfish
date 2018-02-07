package org.batfish.z3.expr;

public class FalseExpr extends BooleanExpr {

  public static final FalseExpr INSTANCE = new FalseExpr();

  private FalseExpr() {}

  @Override
  public void accept(BooleanExprVisitor visitor) {
    visitor.visitFalseExpr(this);
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitFalseExpr(this);
  }
}
