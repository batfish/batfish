package org.batfish.symbolic.state;

import java.io.Serial;

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

  @Serial
  private Object readResolve() {
    return INSTANCE;
  }
}
