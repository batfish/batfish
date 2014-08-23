package batfish.z3.node;

import batfish.z3.Synthesizer;

public abstract class PolicyExpr extends RelExpr {

   public PolicyExpr(String name) {
      super(name);
      for (String arg : Synthesizer.POLICY_VARS) {
         addArgument(new VarIntExpr(arg));
      }
   }
   
}
