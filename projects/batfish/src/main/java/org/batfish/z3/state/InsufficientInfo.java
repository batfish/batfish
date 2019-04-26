package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.StateExprVisitor;

public final class InsufficientInfo implements StateExpr {

  public static final InsufficientInfo INSTANCE = new InsufficientInfo();

  private InsufficientInfo() {}

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitInsufficientInfo();
  }
}
