package org.batfish.z3.expr;

import org.batfish.z3.expr.visitors.ExprVisitor;

public final class FalseExpr extends BooleanExpr {

  public static final FalseExpr INSTANCE = new FalseExpr();

  private FalseExpr() {}

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitFalseExpr();
  }

  @Override
  protected boolean exprEquals(Expr e) {
    return this == e;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }
}
