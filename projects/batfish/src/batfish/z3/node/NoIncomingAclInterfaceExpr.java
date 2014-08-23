package batfish.z3.node;

public class NoIncomingAclInterfaceExpr extends RelExpr {

   public static final String NAME = "N_in_acl";

   public NoIncomingAclInterfaceExpr(IntExpr nodeExpr, IntExpr interfaceExpr) {
      super(NAME);
      addArgument(nodeExpr);
      addArgument(interfaceExpr);
   }
   
   public NoIncomingAclInterfaceExpr(String nodeVar, String interfaceVar) {
      this(new VarIntExpr(nodeVar), new VarIntExpr(interfaceVar));
   }

}
