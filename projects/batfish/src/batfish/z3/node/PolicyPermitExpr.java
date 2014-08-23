package batfish.z3.node;

public class PolicyPermitExpr extends PolicyExpr {

   public static String NAME = "P_policy";

   public PolicyPermitExpr(String policyVar, String interfaceVar) {
      super(NAME);
      addArgument(new VarIntExpr(policyVar));
      addArgument(new VarIntExpr(interfaceVar));
   }

}
