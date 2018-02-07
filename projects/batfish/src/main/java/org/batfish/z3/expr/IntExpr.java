package org.batfish.z3.expr;

public abstract class IntExpr extends Expr {
  public abstract void accept(IntExprVisitor visitor);
}
