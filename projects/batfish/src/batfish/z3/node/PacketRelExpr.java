package batfish.z3.node;

import batfish.z3.Synthesizer;

public abstract class PacketRelExpr extends RelExpr {

   public PacketRelExpr(String name) {
      super(name);
      for (String arg : Synthesizer.PACKET_VARS) {
         addArgument(new VarIntExpr(arg));
      }
   }
   
}
