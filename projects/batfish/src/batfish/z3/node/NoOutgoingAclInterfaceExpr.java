package batfish.z3.node;

public class NoOutgoingAclInterfaceExpr extends RelExpr {

   public static final String NAME = "N_out_acl";

   public NoOutgoingAclInterfaceExpr(IntExpr nodeExpr, IntExpr interfaceExpr) {
      super(NAME);
      addArgument(nodeExpr);
      addArgument(interfaceExpr);
   }
   
   public NoOutgoingAclInterfaceExpr(String nodeVar, String interfaceVar) {
      this(new VarIntExpr(nodeVar), new VarIntExpr(interfaceVar));
   }

}
