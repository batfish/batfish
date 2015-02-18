package org.batfish.z3.node;

public class PreOutEdgeExpr extends PacketRelExpr {

   public static final String BASE_NAME = "R_preout_edge";

   public PreOutEdgeExpr(String node, String outInt, String nextHop,
         String inInt) {
      super(BASE_NAME + "_" + node + "_" + outInt + "_" + nextHop + "_" + inInt);
   }

}
