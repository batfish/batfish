package org.batfish.z3.node;

public class AclDenyExpr extends PolicyExpr {

   public static String BASE_NAME = "D_acl";

   public AclDenyExpr(String nodeName, String aclName) {
      super(BASE_NAME, nodeName, aclName);
   }

}
