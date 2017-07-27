package org.batfish.z3.node;

public class NodeDropNullRouteExpr extends NodePacketRelExpr {

  public static final String BASE_NAME = "R_node_drop_null_route";

  public NodeDropNullRouteExpr(String nodeArg) {
    super(BASE_NAME, nodeArg);
  }
}
