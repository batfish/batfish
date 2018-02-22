package org.batfish.z3.expr;

import org.batfish.z3.expr.visitors.GenericBooleanExprVisitor;

public abstract class BooleanExpr extends Expr {

  public abstract <R> R accept(GenericBooleanExprVisitor<R> visitor);
}
