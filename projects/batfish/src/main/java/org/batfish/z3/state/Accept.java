package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.expr.StateExprVisitor;

public final class Accept implements StateExpr {

  public static final Accept INSTANCE = new Accept();

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitAccept();
  }
}
