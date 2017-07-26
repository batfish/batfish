package org.batfish.z3.node;

public class NodeDropAclExpr extends NodePacketRelExpr {

  public static final String BASE_NAME = "R_node_drop_acl";

  public NodeDropAclExpr(String nodeArg) {
    super(BASE_NAME, nodeArg);
  }
}
