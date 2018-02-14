package org.batfish.z3.expr;

import org.batfish.z3.expr.visitors.ExprPrinter;

public abstract class Statement {
  public abstract <T> T accept(GenericStatementVisitor<T> visitor);

  public abstract void accept(VoidStatementVisitor visitor);

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!getClass().equals(o.getClass())) {
      return false;
    }
    return statementEquals((Statement) o);
  }

  public abstract boolean statementEquals(Statement e);

  @Override
  public abstract int hashCode();

  @Override
  public String toString() {
    return ExprPrinter.print(this);
  }
}
