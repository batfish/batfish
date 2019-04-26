package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;

public class DropAclIn extends StateExpr {

  public static class State {

    public static final State INSTANCE = new State();

    private State() {}
  }

  public static final DropAclIn INSTANCE = new DropAclIn();

  private DropAclIn() {}

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitDropAclIn();
  }
}
