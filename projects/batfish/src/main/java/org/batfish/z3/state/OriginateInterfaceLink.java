package org.batfish.z3.state;

import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.expr.StateExprVisitor;

/** A {@link StateExpr Nod program state} for traffic originating from the link of an interface. */
public final class OriginateInterfaceLink implements StateExpr {

  private final @Nonnull String _hostname;

  private final @Nonnull String _iface;

  public OriginateInterfaceLink(@Nonnull String hostname, @Nonnull String iface) {
    _hostname = hostname;
    _iface = iface;
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OriginateInterfaceLink)) {
      return false;
    }

    OriginateInterfaceLink that = (OriginateInterfaceLink) o;
    return _hostname.equals(that._hostname) && _iface.equals(that._iface);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostname, _iface);
  }
}
