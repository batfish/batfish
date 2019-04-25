package org.batfish.z3.state;

import org.batfish.datamodel.Edge;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

public class PostOutEdge extends StateExpr {

  public static class State extends StateExpr.State {

    public static final State INSTANCE = new State();

    private State() {}

    @Override
    public void accept(StateVisitor visitor) {
      visitor.visitPostOutEdge(this);
    }
  }

  private final String _dstIface;

  private final String _dstNode;

  private final String _srcIface;

  private final String _srcNode;

  public PostOutEdge(Edge edge) {
    this(edge.getNode1(), edge.getInt1(), edge.getNode2(), edge.getInt2());
  }

  public PostOutEdge(String srcNode, String srcIface, String dstNode, String dstIface) {
    _srcNode = srcNode;
    _srcIface = srcIface;
    _dstNode = dstNode;
    _dstIface = dstIface;
  }

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitPostOutEdge(this);
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

  @Override
  public State getState() {
    return State.INSTANCE;
  }
}
