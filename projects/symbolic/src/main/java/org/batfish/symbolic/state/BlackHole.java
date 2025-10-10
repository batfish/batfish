package org.batfish.symbolic.state;

import java.io.Serial;

/**
 * Represents a sink for "impossible" flows. For example, flows leaving an interface that are only
 * valid if routed to a different interface.
 *
 * <p>This can happen because of out-of-order computations in the reachability graph.
 */
public final class BlackHole implements StateExpr {

  public static final BlackHole INSTANCE = new BlackHole();

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
