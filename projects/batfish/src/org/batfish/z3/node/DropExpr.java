package org.batfish.z3.node;

public class DropExpr extends PacketRelExpr {

   public static final DropExpr INSTANCE = new DropExpr();

   public static final String NAME = "R_drop";

   private DropExpr() {
      super(NAME);
   }

}
