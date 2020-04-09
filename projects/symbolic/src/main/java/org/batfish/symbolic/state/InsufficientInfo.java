package org.batfish.symbolic.state;

public final class InsufficientInfo implements StateExpr {

  public static final InsufficientInfo INSTANCE = new InsufficientInfo();

  private InsufficientInfo() {}

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitInsufficientInfo();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
