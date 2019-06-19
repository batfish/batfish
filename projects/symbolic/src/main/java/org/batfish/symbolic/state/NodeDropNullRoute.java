package org.batfish.symbolic.state;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class NodeDropNullRoute extends NodeStateExpr {
  public NodeDropNullRoute(String hostname) {
    super(hostname);
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitNodeDropNullRoute(this);
  }
}
