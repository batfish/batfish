package batfish.z3.node;

public class PreInInterfaceExpr extends PacketRelExpr {

   public static final String NAME = "R_prein_interface";

   public PreInInterfaceExpr(IntExpr nodeExpr, IntExpr interfaceExpr) {
      super(NAME);
      addArgument(nodeExpr);
      addArgument(interfaceExpr);
   }
   
   public PreInInterfaceExpr(String nodeVar, String interfaceVar) {
      this(new VarIntExpr(nodeVar), new VarIntExpr(interfaceVar));
   }

}
