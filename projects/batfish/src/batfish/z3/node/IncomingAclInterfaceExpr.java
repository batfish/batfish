package batfish.z3.node;

public class IncomingAclInterfaceExpr extends RelExpr {

   public static final String NAME = "Y_in_acl";

   public IncomingAclInterfaceExpr(IntExpr nodeExpr, IntExpr interfaceExpr, IntExpr aclExpr) {
      super(NAME);
      addArgument(nodeExpr);
      addArgument(interfaceExpr);
      addArgument(aclExpr);
   }
   
   public IncomingAclInterfaceExpr(String nodeVar, String interfaceVar, String aclVar) {
      this(new VarIntExpr(nodeVar), new VarIntExpr(interfaceVar), new VarIntExpr(aclVar));
   }

}
