package org.batfish.z3.node;

public class NodeDropAclInExpr extends NodePacketRelExpr {

  public static final String BASE_NAME = "R_node_drop_acl_in";

  public NodeDropAclInExpr(String nodeArg) {
    super(BASE_NAME, nodeArg);
  }
}
