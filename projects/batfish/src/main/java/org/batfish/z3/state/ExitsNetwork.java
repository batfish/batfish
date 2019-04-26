package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;

public class ExitsNetwork extends StateExpr {

  public static final ExitsNetwork INSTANCE = new ExitsNetwork();

  private ExitsNetwork() {}

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitExitsNetwork();
  }
}
