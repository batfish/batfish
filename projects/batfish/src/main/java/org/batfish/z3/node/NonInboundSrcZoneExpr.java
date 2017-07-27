package org.batfish.z3.node;

public class NonInboundSrcZoneExpr extends NodeZonePacketRelExpr {

  public static final String BASE_NAME = "R_non_inbound_src_zone";

  public NonInboundSrcZoneExpr(String nodeName, String zoneName) {
    super(BASE_NAME, nodeName, zoneName);
  }
}
