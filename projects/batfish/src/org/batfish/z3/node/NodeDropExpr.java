package org.batfish.z3.node;

public class NodeDropExpr extends NodePacketRelExpr {

   public static final String BASE_NAME = "R_node_drop";

   public NodeDropExpr(String nodeArg) {
      super(BASE_NAME, nodeArg);
   }

}
