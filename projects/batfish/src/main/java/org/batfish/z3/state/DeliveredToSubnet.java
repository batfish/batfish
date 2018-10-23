package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

public class DeliveredToSubnet extends StateExpr {

  public static class State extends StateExpr.State {

    public static final State INSTANCE = new State();

    private State() {}

    @Override
    public void accept(StateVisitor visitor) {
      visitor.visitDeliveredToSubnet(this);
    }
  }

  public static final DeliveredToSubnet INSTANCE = new DeliveredToSubnet();

  private DeliveredToSubnet() {}

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitDeliveredToSubnet(this);
  }

  @Override
  public State getState() {
    return State.INSTANCE;
  }
}
