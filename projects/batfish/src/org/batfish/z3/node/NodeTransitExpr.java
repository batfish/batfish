package org.batfish.z3.node;

public class NodeTransitExpr extends NodePacketRelExpr {

   public static final String BASE_NAME = "R_node_transit";

   public NodeTransitExpr(String nodeArg) {
      super(BASE_NAME, nodeArg);
   }

}
