package org.batfish.z3.node;

public class ReachPostInExpr extends PacketRelExpr {

   private static final String NAME = "ReachPostIn";

   public ReachPostInExpr(String nodeName) {
      super(NAME);
      addArgument(new VarIntExpr(nodeName));
   }

}
