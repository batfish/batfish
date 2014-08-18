package batfish.z3.node;

public class PostInInterfaceExpr extends PacketRelExpr {

   public static final String NAME = "R_postin_interface";

   public PostInInterfaceExpr(IntExpr nodeExpr, IntExpr interfaceExpr) {
      super(NAME);
      addArgument(nodeExpr);
      addArgument(interfaceExpr);
   }
   
   public PostInInterfaceExpr(String nodeVar, String interfaceVar) {
      this(new VarIntExpr(nodeVar), new VarIntExpr(interfaceVar));
   }

}
