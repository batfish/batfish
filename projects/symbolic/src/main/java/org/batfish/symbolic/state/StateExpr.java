package org.batfish.symbolic.state;

import java.io.Serializable;

/** An expression representing parameterized state. */
public interface StateExpr extends Serializable {
  <R> R accept(StateExprVisitor<R> visitor);
}
