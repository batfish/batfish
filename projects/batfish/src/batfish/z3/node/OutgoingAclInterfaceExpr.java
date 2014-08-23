package batfish.z3.node;

public class OutgoingAclInterfaceExpr extends RelExpr {

   public static final String NAME = "Y_out_acl";

   public OutgoingAclInterfaceExpr(IntExpr nodeExpr, IntExpr interfaceExpr, IntExpr aclExpr) {
      super(NAME);
      addArgument(nodeExpr);
      addArgument(interfaceExpr);
      addArgument(aclExpr);
   }
   
   public OutgoingAclInterfaceExpr(String nodeVar, String interfaceVar, String aclVar) {
      this(new VarIntExpr(nodeVar), new VarIntExpr(interfaceVar), new VarIntExpr(aclVar));
   }

}
