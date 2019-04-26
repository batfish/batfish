package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;

public class DeliveredToSubnet extends StateExpr {

  public static final DeliveredToSubnet INSTANCE = new DeliveredToSubnet();

  private DeliveredToSubnet() {}

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitDeliveredToSubnet();
  }
}
