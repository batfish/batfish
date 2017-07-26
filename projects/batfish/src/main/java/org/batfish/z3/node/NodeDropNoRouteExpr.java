package org.batfish.z3.node;

public class NodeDropNoRouteExpr extends NodePacketRelExpr {

  public static final String BASE_NAME = "R_node_drop_no_route";

  public NodeDropNoRouteExpr(String nodeArg) {
    super(BASE_NAME, nodeArg);
  }
}
