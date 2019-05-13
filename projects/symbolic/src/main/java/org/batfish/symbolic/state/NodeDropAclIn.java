package org.batfish.symbolic.state;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class NodeDropAclIn extends NodeStateExpr {

  public NodeDropAclIn(String hostname) {
    super(hostname);
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitNodeDropAclIn(this);
  }
}
