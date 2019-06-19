package org.batfish.symbolic.state;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class PreInInterface extends InterfaceStateExpr {
  public PreInInterface(String hostname, String iface) {
    super(hostname, iface);
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitPreInInterface(this);
  }
}
