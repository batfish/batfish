package org.batfish.z3.node;

public class AclMatchExpr extends PolicyClauseExpr {

   public static String BASE_NAME = "M_acl";

   public AclMatchExpr(String nodeName, String policyName, int clause) {
      super(BASE_NAME, nodeName, policyName, clause);
   }

}
