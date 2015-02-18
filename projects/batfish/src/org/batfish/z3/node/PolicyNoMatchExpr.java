package org.batfish.z3.node;

public class PolicyNoMatchExpr extends PolicyClauseExpr {

   public static String BASE_NAME = "N_policy";

   public PolicyNoMatchExpr(String nodeName, String policyName, int clause) {
      super(BASE_NAME, nodeName, policyName, clause);
   }

}
