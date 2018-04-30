package org.batfish.z3.state;

import javax.annotation.Nonnull;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

public class OriginateInterface extends StateExpr {
  public static class State extends StateExpr.State {

    public static final State INSTANCE = new State();

    private State() {}

    @Override
    public void accept(StateVisitor visitor) {
      visitor.visitOriginateInterface(this);
    }
  }

  private final @Nonnull String _hostname;

  private final @Nonnull String _iface;

  public OriginateInterface(@Nonnull String hostname, @Nonnull String iface) {
    _hostname = hostname;
    _iface = iface;
  }

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitOriginateInterface(this);
  }

  @Nonnull
  public String getHostname() {
    return _hostname;
  }

  @Nonnull
  public String getIface() {
    return _iface;
  }

  @Override
  public State getState() {
    return State.INSTANCE;
  }
}
