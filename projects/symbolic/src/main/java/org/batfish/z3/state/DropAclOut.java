package org.batfish.z3.state;

public final class DropAclOut implements StateExpr {

  public static final DropAclOut INSTANCE = new DropAclOut();

  private DropAclOut() {}

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitDropAclOut();
  }
}
