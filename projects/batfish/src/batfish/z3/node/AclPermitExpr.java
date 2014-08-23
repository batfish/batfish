package batfish.z3.node;

public class AclPermitExpr extends PolicyExpr {

   public static String NAME = "P_acl";

   public AclPermitExpr(String aclVar) {
      super(NAME);
      addArgument(new VarIntExpr(aclVar));
   }

}
