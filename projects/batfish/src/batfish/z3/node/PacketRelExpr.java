package batfish.z3.node;

import batfish.z3.Synthesizer;

public class PacketRelExpr extends RelExpr {

   public PacketRelExpr(String name) {
      super(name);
      for (String arg : Synthesizer.getStdArgs()) {
         addArgument(new VarIntExpr(arg));
      }
   }
   
}
