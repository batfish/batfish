package org.batfish.symbolic.state;

public final class DeliveredToSubnet implements StateExpr {

  public static final DeliveredToSubnet INSTANCE = new DeliveredToSubnet();

  private DeliveredToSubnet() {}

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitDeliveredToSubnet();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
