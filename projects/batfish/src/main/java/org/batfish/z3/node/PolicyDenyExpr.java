package org.batfish.z3.node;

public class PolicyDenyExpr extends PolicyExpr {

   public static String BASE_NAME = "D_policy";

   public PolicyDenyExpr(String hostname, String policyName) {
      super(BASE_NAME, hostname, policyName);
   }

}
