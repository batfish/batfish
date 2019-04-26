package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;

public class PreOutEdge extends StateExpr {

  private final String _dstIface;

  private final String _dstNode;

  private final String _srcIface;

  private final String _srcNode;

  public PreOutEdge(String srcNode, String srcIface, String dstNode, String dstIface) {
    _srcNode = srcNode;
    _srcIface = srcIface;
    _dstNode = dstNode;
    _dstIface = dstIface;
  }

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitPreOutEdge(this);
  }

  public String getDstIface() {
    return _dstIface;
  }

  public String getDstNode() {
    return _dstNode;
  }

  public String getSrcIface() {
    return _srcIface;
  }

  public String getSrcNode() {
    return _srcNode;
  }
}
