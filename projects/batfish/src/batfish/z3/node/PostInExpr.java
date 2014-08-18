package batfish.z3.node;

public class PostInExpr extends PacketRelExpr {

   public static final String NAME = "R_postin";

   public PostInExpr(IntExpr hostnameExpr) {
      super(NAME);
      addArgument(hostnameExpr);
   }

   public PostInExpr(String hostname) {
      this(new VarIntExpr(hostname));
   }

}
