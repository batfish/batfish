package org.batfish.z3.expr;

import org.batfish.z3.expr.visitors.ExprPrinter;
import org.batfish.z3.expr.visitors.ExprVisitor;

public abstract class Expr {

  public abstract void accept(ExprVisitor visitor);

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!getClass().equals(o.getClass())) {
      return false;
    }
    return exprEquals((Expr) o);
  }

  protected abstract boolean exprEquals(Expr e);

  @Override
  public abstract int hashCode();

  @Override
  public String toString() {
    return ExprPrinter.print(this);
  }
}
