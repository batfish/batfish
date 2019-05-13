package org.batfish.symbolic.state;

import javax.annotation.Nonnull;

/** A {@link StateExpr Nod program state} for traffic originating from the link of an interface. */
public final class OriginateInterfaceLink extends InterfaceStateExpr {

  public OriginateInterfaceLink(@Nonnull String hostname, @Nonnull String iface) {
    super(hostname, iface);
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitOriginateInterfaceLink(this);
  }
}
