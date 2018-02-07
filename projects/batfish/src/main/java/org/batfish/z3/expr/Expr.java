package org.batfish.z3.expr;

import org.batfish.z3.expr.visitors.ExprPrinter;

public abstract class Expr {

  public abstract void accept(ExprVisitor visitor);

  @Override
  public String toString() {
    return ExprPrinter.print(this);
  }
}
