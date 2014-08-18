package batfish.z3.node;

public class PreOutExpr extends PacketRelExpr {

   public static final String NAME = "R_preout";

   public PreOutExpr(IntExpr hostnameExpr) {
      super(NAME);
      addArgument(hostnameExpr);
   }

   public PreOutExpr(String hostname) {
      this(new VarIntExpr(hostname));
   }

}
