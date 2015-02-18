package org.batfish.z3.node;

public class PolicyPermitExpr extends PolicyExpr {

   public static String BASE_NAME = "P_policy";

   public PolicyPermitExpr(String nodeName, String policyName) {
      super(BASE_NAME, nodeName, policyName);
   }

}
