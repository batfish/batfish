package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;

public class DropAclOut extends StateExpr {

  public static final DropAclOut INSTANCE = new DropAclOut();

  private DropAclOut() {}

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitDropAclOut();
  }
}
