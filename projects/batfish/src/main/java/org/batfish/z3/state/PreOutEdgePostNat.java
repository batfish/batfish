package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;

public class PreOutEdgePostNat extends StateExpr {

  public static class State {

    public static final State INSTANCE = new State();

    private State() {}
  }

  private final String _dstIface;

  private final String _dstNode;

  private final String _srcIface;

  private final String _srcNode;

  public PreOutEdgePostNat(String srcNode, String srcIface, String dstNode, String dstIface) {
    _srcNode = srcNode;
    _srcIface = srcIface;
    _dstNode = dstNode;
    _dstIface = dstIface;
  }

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitPreOutEdgePostNat(this);
  }

  public String getDstNode() {
    return _dstNode;
  }

  public String getDstIface() {
    return _dstIface;
  }

  public String getSrcIface() {
    return _srcIface;
  }

  public String getSrcNode() {
    return _srcNode;
  }
}
