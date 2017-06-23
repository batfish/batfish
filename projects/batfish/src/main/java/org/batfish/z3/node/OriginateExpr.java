package org.batfish.z3.node;

public class OriginateExpr extends NodePacketRelExpr {

   private static final String BASE_NAME = "R_originate";

   public OriginateExpr(String nodeName) {
      super(BASE_NAME, nodeName);
   }

}
