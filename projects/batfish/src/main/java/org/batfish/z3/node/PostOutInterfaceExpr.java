package org.batfish.z3.node;

public class PostOutInterfaceExpr extends InterfacePacketRelExpr {

   public static final String BASE_NAME = "R_postout_interface";

   public PostOutInterfaceExpr(String nodeName, String interfaceName) {
      super(BASE_NAME, nodeName, interfaceName);
   }

}
