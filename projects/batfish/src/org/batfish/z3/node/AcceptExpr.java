package org.batfish.z3.node;

public class AcceptExpr extends PacketRelExpr {

   public static final AcceptExpr INSTANCE = new AcceptExpr();

   public static final String NAME = "R_accept";

   private AcceptExpr() {
      super(NAME);
   }

}
