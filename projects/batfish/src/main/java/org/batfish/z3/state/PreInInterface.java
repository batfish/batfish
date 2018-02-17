package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.StateExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

public class PreInInterface extends StateExpr {

  public static class State extends StateExpr.State {

    public static final State INSTANCE = new State();

    private State() {}

    @Override
    public void accept(StateVisitor visitor) {
      visitor.visitPreInInterface(this);
    }
  }

  private final String _hostname;

  private final String _iface;

  public PreInInterface(String hostname, String iface) {
    _hostname = hostname;
    _iface = iface;
  }

  @Override
  public void accept(StateExprVisitor visitor) {
    visitor.visitPreInInterface(this);
  }

  public String getHostname() {
    return _hostname;
  }

  public String getIface() {
    return _iface;
  }
}
