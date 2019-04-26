package org.batfish.z3.state;

import javax.annotation.Nonnull;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;

/** A {@link StateExpr Nod program state} for traffic originating from the link of an interface. */
public final class OriginateInterfaceLink extends StateExpr {
  public static class State {

    public static final State INSTANCE = new State();

    private State() {}
  }

  private final @Nonnull String _hostname;

  private final @Nonnull String _iface;

  public OriginateInterfaceLink(@Nonnull String hostname, @Nonnull String iface) {
    _hostname = hostname;
    _iface = iface;
  }

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitOriginateInterfaceLink(this);
  }

  @Nonnull
  public String getHostname() {
    return _hostname;
  }

  @Nonnull
  public String getIface() {
    return _iface;
  }
}
