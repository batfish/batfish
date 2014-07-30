package batfish.z3.node;

public abstract class Expr {
   
   protected ExprPrinter _printer;
   
   public void print(StringBuilder sb, int indent) {
      _printer.print(sb, indent);
   }
   
   public Expr simplify() {
      return this;
   };
   
}
