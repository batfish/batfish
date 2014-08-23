package batfish.z3.node;

public class AclDenyExpr extends PolicyExpr {

   public static String NAME = "D_acl";

   public AclDenyExpr(String aclVar) {
      super(NAME);
      addArgument(new VarIntExpr(aclVar));
   }

}
