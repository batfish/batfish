package org.batfish.z3.node;

public class OriginateVrfExpr extends NodePacketRelExpr {

   private static final String BASE_NAME = "R_originate";

   public OriginateVrfExpr(String nodeName, String vrf) {
      super(BASE_NAME, nodeName + "_" + vrf);
   }

}
