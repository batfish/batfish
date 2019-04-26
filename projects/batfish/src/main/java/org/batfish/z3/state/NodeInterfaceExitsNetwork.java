package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;

public class NodeInterfaceExitsNetwork extends StateExpr {

  public static class State {

    public static final State INSTANCE = new State();

    private State() {}
  }

  private final String _hostname;

  private final String _iface;

  public NodeInterfaceExitsNetwork(String hostname, String iface) {
    _hostname = hostname;
    _iface = iface;
  }

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitNodeInterfaceExitsNetwork(this);
  }

  public String getHostname() {
    return _hostname;
  }

  public String getIface() {
    return _iface;
  }
}
