package org.batfish.z3.node;

import org.batfish.representation.Edge;

public class PreOutEdgeExpr extends PacketRelExpr {

   public static final String BASE_NAME = "R_preout_edge";

   public PreOutEdgeExpr(Edge edge) {
      this(edge.getNode1(), edge.getInt1(), edge.getNode2(), edge.getInt2());
   }

   public PreOutEdgeExpr(String node, String outInt, String nextHop,
         String inInt) {
      super(BASE_NAME + "_" + node + "_" + outInt + "_" + nextHop + "_" + inInt);
   }

}
