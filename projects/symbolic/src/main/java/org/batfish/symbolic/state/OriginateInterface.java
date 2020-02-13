package org.batfish.symbolic.state;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class OriginateInterface extends InterfaceStateExpr {
  public OriginateInterface(String hostname, String ifaceName) {
    super(hostname, ifaceName);
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitOriginateInterface(this);
  }
}
