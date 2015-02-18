package org.batfish.z3.node;

public class AclNoMatchExpr extends PolicyClauseExpr {

   public static String BASE_NAME = "N_acl";

   public AclNoMatchExpr(String nodeName, String policyName, int clause) {
      super(BASE_NAME, nodeName, policyName, clause);
   }

}
