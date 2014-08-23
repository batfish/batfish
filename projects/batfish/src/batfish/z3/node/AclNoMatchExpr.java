package batfish.z3.node;

public class AclNoMatchExpr extends PolicyExpr {

   public static String NAME = "N_acl";

   public AclNoMatchExpr(String aclVar, String lineVar) {
      super(NAME);
      addArgument(new VarIntExpr(aclVar));
      addArgument(new VarIntExpr(lineVar));
   }

}
