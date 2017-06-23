package org.batfish.z3.node;

public class PostInVrfExpr extends PacketRelExpr {

   public static final String BASE_NAME = "R_postin_vrf";

   public PostInVrfExpr(String nodeName, String vrfName) {
      super(BASE_NAME + "_" + nodeName + "_" + vrfName);
   }

}
