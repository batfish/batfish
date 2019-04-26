package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.StateExprVisitor;

public final class DeliveredToSubnet implements StateExpr {

  public static final DeliveredToSubnet INSTANCE = new DeliveredToSubnet();

  private DeliveredToSubnet() {}

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitDeliveredToSubnet();
  }
}
