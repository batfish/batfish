package org.batfish.symbolic.state;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class InterfaceAccept extends InterfaceStateExpr {
  public InterfaceAccept(String hostname, String ifaceName) {
    super(hostname, ifaceName);
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitInterfaceAccept(this);
  }
}
