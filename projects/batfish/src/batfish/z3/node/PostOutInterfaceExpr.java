package batfish.z3.node;

public class PostOutInterfaceExpr extends PacketRelExpr {

   public static final String NAME = "R_postout_interface";

   public PostOutInterfaceExpr(IntExpr nodeExpr, IntExpr interfaceExpr) {
      super(NAME);
      addArgument(nodeExpr);
      addArgument(interfaceExpr);
   }
   
   public PostOutInterfaceExpr(String nodeVar, String interfaceVar) {
      this(new VarIntExpr(nodeVar), new VarIntExpr(interfaceVar));
   }

}
