package org.batfish.z3.node;

public class NonInboundSrcInterfaceExpr extends InterfacePacketRelExpr {

  public static final String BASE_NAME = "R_non_inbound_src_interface";

  public NonInboundSrcInterfaceExpr(String nodeName, String interfaceName) {
    super(BASE_NAME, nodeName, interfaceName);
  }
}
