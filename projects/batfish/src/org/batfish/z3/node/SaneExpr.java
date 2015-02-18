package org.batfish.z3.node;

public class SaneExpr extends PacketRelExpr {

   public static final SaneExpr INSTANCE = new SaneExpr();

   private static final String NAME = "Sane";

   private SaneExpr() {
      super(NAME);
   }

}
