package org.batfish.z3.node;

public class PreOutExpr extends NodePacketRelExpr {

   public static final String BASE_NAME = "R_preout";

   public PreOutExpr(String hostname) {
      super(BASE_NAME, hostname);
   }

}
