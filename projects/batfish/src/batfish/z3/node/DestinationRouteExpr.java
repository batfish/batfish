package batfish.z3.node;

public class DestinationRouteExpr extends PacketRelExpr {

   public static final String NAME = "R_destroute";

   public DestinationRouteExpr(IntExpr hostnameExpr) {
      super(NAME);
      addArgument(hostnameExpr);
   }

   public DestinationRouteExpr(String hostname) {
      this(new VarIntExpr(hostname));
   }

}
