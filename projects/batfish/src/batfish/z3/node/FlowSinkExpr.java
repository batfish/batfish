package batfish.z3.node;

public class FlowSinkExpr extends RelExpr {

   public static final String NAME = "I_flow_sink";

   public FlowSinkExpr(IntExpr nodeExpr, IntExpr interfaceExpr) {
      super(NAME);
      addArgument(nodeExpr);
      addArgument(interfaceExpr);
   }
   
   public FlowSinkExpr(String nodeVar, String interfaceVar) {
      this(new VarIntExpr(nodeVar), new VarIntExpr(interfaceVar));
   }

}
