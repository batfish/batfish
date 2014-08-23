package batfish.z3.node;

public class PolicyRoutedExpr extends RelExpr {

   public static final String NAME = "I_policy_routed";

   public PolicyRoutedExpr(IntExpr nodeExpr, IntExpr interfaceExpr) {
      super(NAME);
      addArgument(nodeExpr);
      addArgument(interfaceExpr);
   }
   
   public PolicyRoutedExpr(String nodeVar, String interfaceVar) {
      this(new VarIntExpr(nodeVar), new VarIntExpr(interfaceVar));
   }

}
