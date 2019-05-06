package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.expr.StateExprVisitor;

public final class ExitsNetwork implements StateExpr {

  public static final ExitsNetwork INSTANCE = new ExitsNetwork();

  private ExitsNetwork() {}

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitExitsNetwork();
  }
}
