package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.expr.StateExprVisitor;

public final class Query implements StateExpr {

  public static final Query INSTANCE = new Query();

  private Query() {}

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitQuery();
  }
}
