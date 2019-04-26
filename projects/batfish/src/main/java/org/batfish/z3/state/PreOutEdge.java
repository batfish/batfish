package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.StateExprVisitor;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
public final class PreOutEdge implements StateExpr {

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
  public <R> R accept(StateExprVisitor<R> visitor) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PreOutEdge)) {
      return false;
    }
    PreOutEdge that = (PreOutEdge) o;
    return _dstIface.equals(that._dstIface)
        && _dstNode.equals(that._dstNode)
        && _srcIface.equals(that._srcIface)
        && _srcNode.equals(that._srcNode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_dstIface, _dstNode, _srcIface, _srcNode);
  }
}
