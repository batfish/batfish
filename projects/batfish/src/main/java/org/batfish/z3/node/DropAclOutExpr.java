package org.batfish.z3.node;

public class DropAclOutExpr extends PacketRelExpr {

  public static final DropAclOutExpr INSTANCE = new DropAclOutExpr();

  public static final String NAME = "R_drop_acl_out";

  private DropAclOutExpr() {
    super(NAME);
  }
}
