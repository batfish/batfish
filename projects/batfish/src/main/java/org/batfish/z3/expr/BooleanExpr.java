package org.batfish.z3.expr;

import org.batfish.z3.expr.visitors.BooleanExprVisitor;

public abstract class BooleanExpr extends Expr {
  public abstract void accept(BooleanExprVisitor visitor);
}
