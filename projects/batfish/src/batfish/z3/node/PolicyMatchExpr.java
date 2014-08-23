package batfish.z3.node;

public class PolicyMatchExpr extends PolicyExpr {

   public static String NAME = "M_policy";

   public PolicyMatchExpr(String policyVar, String clauseVar) {
      super(NAME);
      addArgument(new VarIntExpr(policyVar));
      addArgument(new VarIntExpr(clauseVar));
   }

}
