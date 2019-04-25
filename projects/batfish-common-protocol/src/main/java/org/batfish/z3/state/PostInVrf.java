package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

public class PostInVrf extends StateExpr {

  public static class State extends StateExpr.State {

    public static final State INSTANCE = new State();

    private State() {}

    @Override
    public void accept(StateVisitor visitor) {
      visitor.visitPostInVrf(this);
    }
  }

  private final String _hostname;

  private final String _vrf;

  public PostInVrf(String hostname, String vrf) {
    _hostname = hostname;
    _vrf = vrf;
  }

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitPostInVrf(this);
  }

  public String getHostname() {
    return _hostname;
  }

  @Override
  public State getState() {
    return State.INSTANCE;
  }

  public String getVrf() {
    return _vrf;
  }
}
