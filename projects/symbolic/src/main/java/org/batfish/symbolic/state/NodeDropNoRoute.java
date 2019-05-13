package org.batfish.symbolic.state;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class NodeDropNoRoute extends NodeStateExpr {
  public NodeDropNoRoute(String hostname) {
    super(hostname);
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitNodeDropNoRoute(this);
  }
}
