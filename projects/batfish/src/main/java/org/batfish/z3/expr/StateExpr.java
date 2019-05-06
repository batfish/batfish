package org.batfish.z3.expr;

/** An expression representing parameterized state. */
public interface StateExpr {
  <R> R accept(StateExprVisitor<R> visitor);
}
