package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.expr.StateExprVisitor;

public final class DropAclIn implements StateExpr {

  public static final DropAclIn INSTANCE = new DropAclIn();

  private DropAclIn() {}

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitDropAclIn();
  }
}
