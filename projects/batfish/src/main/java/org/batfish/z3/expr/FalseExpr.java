package org.batfish.z3.expr;

import org.batfish.z3.expr.visitors.BooleanExprVisitor;
import org.batfish.z3.expr.visitors.ExprVisitor;

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

  @Override
  public boolean exprEquals(Expr e) {
    return true;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }
}
