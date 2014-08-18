package batfish.z3.node;

public class NodeDropExpr extends PacketRelExpr {

   public static final String NAME = "R_node_drop";
   
   public NodeDropExpr(String nodeArg) {
      super(NAME);
      addArgument(new VarIntExpr(nodeArg));
   }

}
