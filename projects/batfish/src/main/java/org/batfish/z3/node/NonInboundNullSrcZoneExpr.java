package org.batfish.z3.node;

public class NonInboundNullSrcZoneExpr extends NodePacketRelExpr {

  public static final String BASE_NAME = "R_non_inbound_null_src_zone";

  public NonInboundNullSrcZoneExpr(String nodeName) {
    super(BASE_NAME, nodeName);
  }
}
