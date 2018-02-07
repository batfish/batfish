package org.batfish.z3.expr;

public class TrueExpr extends BooleanExpr {

  public static final TrueExpr INSTANCE = new TrueExpr();

  private TrueExpr() {}

  @Override
  public void accept(BooleanExprVisitor visitor) {
    visitor.visitTrueExpr(this);
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitTrueExpr(this);
  }
}
