package org.batfish.symbolic.state;

public final class DropAclIn implements StateExpr {

  public static final DropAclIn INSTANCE = new DropAclIn();

  private DropAclIn() {}

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitDropAclIn();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
