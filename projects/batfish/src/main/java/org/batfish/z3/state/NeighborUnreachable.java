package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.expr.StateExprVisitor;

public class NeighborUnreachable implements StateExpr {

  public static final NeighborUnreachable INSTANCE = new NeighborUnreachable();

  private NeighborUnreachable() {}

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitNeighborUnreachable();
  }
}
