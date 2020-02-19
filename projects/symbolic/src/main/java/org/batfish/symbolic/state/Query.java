package org.batfish.symbolic.state;

public final class Query implements StateExpr {

  public static final Query INSTANCE = new Query();

  private Query() {}

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitQuery();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
