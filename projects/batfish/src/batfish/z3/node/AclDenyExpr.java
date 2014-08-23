package batfish.z3.node;

public class AclDenyExpr extends PolicyExpr {

   public static String NAME = "D_acl";

   public AclDenyExpr(IntExpr aclExpr) {
      super(NAME);
      addArgument(aclExpr);
   }

   public AclDenyExpr(String aclVar) {
      this(new VarIntExpr(aclVar));
   }

}
