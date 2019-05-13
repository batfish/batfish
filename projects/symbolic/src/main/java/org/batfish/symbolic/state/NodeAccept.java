package org.batfish.symbolic.state;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class NodeAccept extends NodeStateExpr {

  public NodeAccept(String hostname) {
    super(hostname);
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitNodeAccept(this);
  }
}
