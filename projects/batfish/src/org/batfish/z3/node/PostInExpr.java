package org.batfish.z3.node;

public class PostInExpr extends NodePacketRelExpr {

   public static final String BASE_NAME = "R_postin";

   public PostInExpr(String hostname) {
      super(BASE_NAME, hostname);
   }

}
