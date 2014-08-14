package batfish.z3.node;

public class VarIntExpr extends IntExpr {

   private String _var;

   public VarIntExpr(String var) {
      _var = var;
      _printer = new SimpleExprPrinter(_var);
   }

   @Override
   public boolean equals(Object o) {
      if (o instanceof VarIntExpr) {
         VarIntExpr rhs = (VarIntExpr) o;
         return _var.equals(rhs._var);
      }
      return false;
   }

   @Override
   public int hashCode() {
      return _var.hashCode();
   }

}
