package batfish.z3.node;

public class IdExpr extends Expr {

   private String _id;

   public IdExpr(String id) {
      _id = id;
   }

   public String getId() {
      return _id;
   }
   
   @Override
   public void print(StringBuffer sb, int indent) {
      sb.append(_id);
   }

}
