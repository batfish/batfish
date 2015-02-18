package org.batfish.z3.node;

public class DestinationRouteExpr extends NodePacketRelExpr {

   public static final String BASE_NAME = "R_destroute";

   public DestinationRouteExpr(String hostname) {
      super(BASE_NAME, hostname);
   }

}
