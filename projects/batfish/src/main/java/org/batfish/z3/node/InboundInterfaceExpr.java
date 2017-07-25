package org.batfish.z3.node;

public class InboundInterfaceExpr extends InterfacePacketRelExpr {

  public static final String BASE_NAME = "R_inbound_interface";

  public InboundInterfaceExpr(String nodeName, String interfaceName) {
    super(BASE_NAME, nodeName, interfaceName);
  }
}
