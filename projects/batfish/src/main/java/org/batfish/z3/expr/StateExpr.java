package org.batfish.z3.expr;

import org.batfish.z3.state.visitors.StateExprVisitor;

/** An expression representing parameterized state. */
public interface StateExpr {
  <R> R accept(StateExprVisitor<R> visitor);
}
