package org.batfish.z3.node;

public class DropNoRouteExpr extends PacketRelExpr {

  public static final DropNoRouteExpr INSTANCE = new DropNoRouteExpr();

  public static final String NAME = "R_drop_no_route";

  private DropNoRouteExpr() {
    super(NAME);
  }
}
