package org.batfish.z3.expr;

import org.batfish.z3.expr.visitors.IntExprVisitor;

public abstract class IntExpr extends Expr {
  public abstract void accept(IntExprVisitor visitor);

  public abstract int numBits();
}
