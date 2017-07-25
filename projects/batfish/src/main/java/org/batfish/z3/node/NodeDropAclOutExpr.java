package org.batfish.z3.node;

public class NodeDropAclOutExpr extends NodePacketRelExpr {

  public static final String BASE_NAME = "R_node_drop_acl_out";

  public NodeDropAclOutExpr(String nodeArg) {
    super(BASE_NAME, nodeArg);
  }
}
