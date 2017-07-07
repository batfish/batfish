package org.batfish.z3.node;

public class PreOutInterfaceExpr extends InterfacePacketRelExpr {

   public static final String BASE_NAME = "R_preout_interface";

   public PreOutInterfaceExpr(String nodeName, String interfaceName) {
      super(BASE_NAME, nodeName, interfaceName);
   }

}
