package org.batfish.z3.node;

public abstract class PolicyClauseExpr extends PacketRelExpr {

   public PolicyClauseExpr(String baseName, String hostname, String policyName,
         int clause) {
      super(baseName + "_" + hostname + "_" + policyName + "_" + clause);
   }

}
