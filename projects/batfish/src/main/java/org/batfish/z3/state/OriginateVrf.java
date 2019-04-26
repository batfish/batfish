package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;

public class OriginateVrf extends StateExpr {

  public static class State {

    public static final State INSTANCE = new State();

    private State() {}
  }

  private final String _hostname;

  private final String _vrf;

  public OriginateVrf(String hostname, String vrf) {
    _hostname = hostname;
    _vrf = vrf;
  }

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitOriginateVrf(this);
  }

  public String getHostname() {
    return _hostname;
  }

  public String getVrf() {
    return _vrf;
  }
}
