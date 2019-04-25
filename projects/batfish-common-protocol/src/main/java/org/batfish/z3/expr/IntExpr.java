package org.batfish.z3.expr;

import org.batfish.z3.expr.visitors.GenericIntExprVisitor;
import org.batfish.z3.expr.visitors.IntExprVisitor;

public abstract class IntExpr extends Expr {
  public abstract <R> R accept(GenericIntExprVisitor<R> visitor);

  public abstract void accept(IntExprVisitor visitor);

  public abstract int numBits();
}
