package batfish.z3.node;

public class PolicyDenyExpr extends PolicyExpr {

   public static String NAME = "D_policy";

   public PolicyDenyExpr(String policyVar) {
      super(NAME);
      addArgument(new VarIntExpr(policyVar));
   }

}
