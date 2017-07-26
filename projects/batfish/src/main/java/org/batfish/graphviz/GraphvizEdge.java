package org.batfish.graphviz;

import org.batfish.common.Pair;

public class GraphvizEdge extends Pair<GraphvizNode, GraphvizNode> {

  /** */
  private static final long serialVersionUID = 1L;

  public GraphvizEdge(GraphvizNode t1, GraphvizNode t2) {
    super(t1, t2);
  }

  public GraphvizNode getFromNode() {
    return _first;
  }

  public GraphvizNode getToNode() {
    return _second;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(_first.getName() + " -> " + _second.getName() + ";");
    return sb.toString();
  }
}
