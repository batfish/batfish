package org.batfish.z3.node;

public class DropNullRouteExpr extends PacketRelExpr {

  public static final DropNullRouteExpr INSTANCE = new DropNullRouteExpr();

  public static final String NAME = "R_drop_null_route";

  private DropNullRouteExpr() {
    super(NAME);
  }
}
