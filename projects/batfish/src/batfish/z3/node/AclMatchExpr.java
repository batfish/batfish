package batfish.z3.node;

public class AclMatchExpr extends PolicyExpr {

   public static String NAME = "M_acl";

   public AclMatchExpr(String aclVar, String lineVar) {
      super(NAME);
      addArgument(new VarIntExpr(aclVar));
      addArgument(new VarIntExpr(lineVar));
   }

}
