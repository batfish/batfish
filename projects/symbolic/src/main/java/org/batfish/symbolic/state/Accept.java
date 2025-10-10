package org.batfish.symbolic.state;

import java.io.Serial;

public final class Accept implements StateExpr {

  public static final Accept INSTANCE = new Accept();

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitAccept();
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
