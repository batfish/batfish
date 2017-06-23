package org.batfish.z3.node;

public abstract class InterfacePacketRelExpr extends PacketRelExpr {

   public InterfacePacketRelExpr(String baseName, String nodeName,
         String interfaceName) {
      super(baseName + "_" + nodeName + "_" + interfaceName);
   }

}
