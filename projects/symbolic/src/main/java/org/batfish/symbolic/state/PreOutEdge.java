package org.batfish.symbolic.state;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class PreOutEdge extends EdgeStateExpr {
  public PreOutEdge(String srcNode, String srcIface, String dstNode, String dstIface) {
    super(srcNode, srcIface, dstNode, dstIface);
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitPreOutEdge(this);
  }
}
