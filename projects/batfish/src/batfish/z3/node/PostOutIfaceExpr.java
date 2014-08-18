package batfish.z3.node;

public class PostOutIfaceExpr extends PacketRelExpr {

   private static final String NAME = "R_postout_iface";
   
   public PostOutIfaceExpr(String nodeVar, String interfaceVar) {
      super(NAME);
      addArgument(new VarIntExpr(nodeVar));
      addArgument(new VarIntExpr(interfaceVar));
   }

}
