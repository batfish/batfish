package batfish.z3.node;

public class NotPolicyRoutedExpr extends RelExpr {

   public static final String NAME = "N_policy_routed";

   public NotPolicyRoutedExpr(IntExpr nodeExpr, IntExpr interfaceExpr) {
      super(NAME);
      addArgument(nodeExpr);
      addArgument(interfaceExpr);
   }
   
   public NotPolicyRoutedExpr(String nodeVar, String interfaceVar) {
      this(new VarIntExpr(nodeVar), new VarIntExpr(interfaceVar));
   }

}
