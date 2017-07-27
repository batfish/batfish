package org.batfish.z3.node;

public class DropAclInExpr extends PacketRelExpr {

  public static final DropAclInExpr INSTANCE = new DropAclInExpr();

  public static final String NAME = "R_drop_acl_in";

  private DropAclInExpr() {
    super(NAME);
  }
}
