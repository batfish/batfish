package org.batfish.symbolic.state;

import java.io.Serial;

public final class DropNoRoute implements StateExpr {

  public static final DropNoRoute INSTANCE = new DropNoRoute();

  private DropNoRoute() {}

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitDropNoRoute();
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
