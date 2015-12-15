package org.batfish.dot;

import org.batfish.common.Pair;

public class Edge extends Pair<Node, Node> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public Edge(Node t1, Node t2) {
      super(t1, t2);
   }

   public Node fromNode() {
      return _t1;
   }

   public Node toNode() {
      return _t2;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(_t1.getName() + " -> " + _t2.getName() + ";");
      return sb.toString();
   }

}
