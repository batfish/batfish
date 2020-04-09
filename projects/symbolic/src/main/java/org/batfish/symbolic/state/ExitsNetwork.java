package org.batfish.symbolic.state;

public final class ExitsNetwork implements StateExpr {

  public static final ExitsNetwork INSTANCE = new ExitsNetwork();

  private ExitsNetwork() {}

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitExitsNetwork();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
