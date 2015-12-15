package org.batfish.graphviz;

import org.batfish.common.Pair;

public class GraphvizEdge extends Pair<GraphvizNode, GraphvizNode> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public GraphvizEdge(GraphvizNode t1, GraphvizNode t2) {
      super(t1, t2);
   }

   public GraphvizNode getFromNode() {
      return _t1;
   }

   public GraphvizNode getToNode() {
      return _t2;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(_t1.getName() + " -> " + _t2.getName() + ";");
      return sb.toString();
   }

}
