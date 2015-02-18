package org.batfish.z3.node;

public class AclPermitExpr extends PolicyExpr {

   public static String BASE_NAME = "P_acl";

   public AclPermitExpr(String nodeName, String aclName) {
      super(BASE_NAME, nodeName, aclName);
   }

}
