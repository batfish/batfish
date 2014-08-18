package batfish.z3.node;

public class PreOutInterfaceExpr extends PacketRelExpr {

   public static final String NAME = "R_preout_interface";

   public PreOutInterfaceExpr(IntExpr nodeExpr, IntExpr interfaceExpr) {
      super(NAME);
      addArgument(nodeExpr);
      addArgument(interfaceExpr);
   }
   
   public PreOutInterfaceExpr(String nodeVar, String interfaceVar) {
      this(new VarIntExpr(nodeVar), new VarIntExpr(interfaceVar));
   }

}
