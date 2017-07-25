package org.batfish.z3.node;

public abstract class NodeZonePacketRelExpr extends PacketRelExpr {

  public NodeZonePacketRelExpr(String baseName, String nodeName, String zoneName) {
    super(baseName + "_" + nodeName + "_" + zoneName);
  }
}
