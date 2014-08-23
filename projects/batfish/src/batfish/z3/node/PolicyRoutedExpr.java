package batfish.z3.node;

public class PolicyRoutedExpr extends RelExpr {

   public static final String NAME = "Y_policy_routed";

   public PolicyRoutedExpr(IntExpr nodeExpr, IntExpr interfaceExpr, IntExpr policyExpr) {
      super(NAME);
      addArgument(nodeExpr);
      addArgument(interfaceExpr);
      addArgument(policyExpr);
   }
   
   public PolicyRoutedExpr(String nodeVar, String interfaceVar, String policyVar) {
      this(new VarIntExpr(nodeVar), new VarIntExpr(interfaceVar), new VarIntExpr(policyVar));
   }

}
