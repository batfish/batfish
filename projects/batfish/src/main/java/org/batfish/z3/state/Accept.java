package org.batfish.z3.state;

public final class Accept implements StateExpr {

  public static final Accept INSTANCE = new Accept();

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitAccept();
  }
}
