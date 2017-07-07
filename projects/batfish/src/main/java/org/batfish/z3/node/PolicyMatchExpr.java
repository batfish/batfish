package org.batfish.z3.node;

public class PolicyMatchExpr extends PolicyClauseExpr {

   public static String BASE_NAME = "M_policy";

   public PolicyMatchExpr(String nodeName, String policyName, int clause) {
      super(BASE_NAME, nodeName, policyName, clause);
   }

}
