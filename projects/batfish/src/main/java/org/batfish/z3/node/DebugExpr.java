package org.batfish.z3.node;

public class DebugExpr extends PacketRelExpr {

  public static final DebugExpr INSTANCE = new DebugExpr();

  public static final String NAME = "R_debug";

  public DebugExpr() {
    super(NAME);
  }
}
