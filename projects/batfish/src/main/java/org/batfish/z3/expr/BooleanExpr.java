package org.batfish.z3.expr;

public abstract class BooleanExpr extends Expr {
  public abstract void accept(BooleanExprVisitor visitor);
}
