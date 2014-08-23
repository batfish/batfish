package batfish.z3.node;

public class PolicyNoMatchExpr extends PolicyExpr {

   public static String NAME = "N_policy";

   public PolicyNoMatchExpr(String policyVar, String clauseVar) {
      super(NAME);
      addArgument(new VarIntExpr(policyVar));
      addArgument(new VarIntExpr(clauseVar));
   }

}
