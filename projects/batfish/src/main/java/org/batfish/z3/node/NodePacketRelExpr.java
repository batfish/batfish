package org.batfish.z3.node;

public abstract class NodePacketRelExpr extends PacketRelExpr {

   public NodePacketRelExpr(String baseName, String nodeName) {
      super(baseName + "_" + nodeName);
   }

}
