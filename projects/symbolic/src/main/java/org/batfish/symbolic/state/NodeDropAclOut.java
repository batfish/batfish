package org.batfish.symbolic.state;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class NodeDropAclOut extends NodeStateExpr {

  public NodeDropAclOut(String hostname) {
    super(hostname);
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitNodeDropAclOut(this);
  }
}
