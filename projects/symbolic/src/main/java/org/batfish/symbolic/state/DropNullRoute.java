package org.batfish.symbolic.state;

public final class DropNullRoute implements StateExpr {
  public static final DropNullRoute INSTANCE = new DropNullRoute();

  private DropNullRoute() {}

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitDropNullRoute();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
