package batfish.z3.node;

import java.util.Collections;
import java.util.Set;

public abstract class Expr {

   protected ExprPrinter _printer;

   public Set<String> getRelations() {
      return Collections.emptySet();
   };

   public Set<String> getVariables() {
      return Collections.emptySet();
   }

   public void print(StringBuilder sb, int indent) {
      _printer.print(sb, indent);
   }

   public Expr simplify() {
      return this;
   }

}
