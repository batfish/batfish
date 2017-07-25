package org.batfish.z3.node;

public class DropAclExpr extends PacketRelExpr {

  public static final DropAclExpr INSTANCE = new DropAclExpr();

  public static final String NAME = "R_drop_acl";

  private DropAclExpr() {
    super(NAME);
  }
}
