package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

public class NodeNeighborUnreachableOrExitsNetwork extends StateExpr {

  public static class State extends StateExpr.State {

    public static final State INSTANCE = new State();

    private State() {}

    @Override
    public void accept(StateVisitor visitor) {
      visitor.visitNodeNeighborUnreachableOrExitsNetwork(this);
    }
  }

  private final String _hostname;

  public NodeNeighborUnreachableOrExitsNetwork(String hostname) {
    _hostname = hostname;
  }

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitNodeNeighborUnreachableOrExitsNetwork(this);
  }

  public String getHostname() {
    return _hostname;
  }

  @Override
  public State getState() {
    return State.INSTANCE;
  }
}
