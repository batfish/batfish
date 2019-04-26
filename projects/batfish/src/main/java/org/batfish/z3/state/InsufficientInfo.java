package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;

public class InsufficientInfo extends StateExpr {

  public static final InsufficientInfo INSTANCE = new InsufficientInfo();

  private InsufficientInfo() {}

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitInsufficientInfo();
  }
}
