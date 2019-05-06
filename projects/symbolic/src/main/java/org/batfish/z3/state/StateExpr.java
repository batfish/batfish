package org.batfish.z3.state;

/** An expression representing parameterized state. */
public interface StateExpr {
  <R> R accept(StateExprVisitor<R> visitor);
}
