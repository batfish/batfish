package batfish.z3.node;

public class NodeAcceptExpr extends PacketRelExpr {

   public static final String NAME = "R_node_accept";
   
   public NodeAcceptExpr(String nodeArg) {
      super(NAME);
      addArgument(new VarIntExpr(nodeArg));
   }

}
