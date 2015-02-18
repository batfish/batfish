package org.batfish.z3.node;

public class PreInInterfaceExpr extends InterfacePacketRelExpr {

   public static final String BASE_NAME = "R_prein_interface";

   public PreInInterfaceExpr(String nodeName, String interfaceName) {
      super(BASE_NAME, nodeName, interfaceName);
   }

}
