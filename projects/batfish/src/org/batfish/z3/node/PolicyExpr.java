package org.batfish.z3.node;

public abstract class PolicyExpr extends PacketRelExpr {

   public PolicyExpr(String baseName, String hostname, String policyName) {
      super(baseName + "_" + hostname + "_" + policyName);
   }

}
