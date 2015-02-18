package org.batfish.z3.node;

public class NodeAcceptExpr extends NodePacketRelExpr {

   public static final String BASE_NAME = "R_node_accept";

   public NodeAcceptExpr(String nodeArg) {
      super(BASE_NAME, nodeArg);
   }

}
