package batfish.z3.node;

import java.util.ArrayList;
import java.util.List;

public class RelExpr extends BooleanExpr implements ComplexExpr {

   private List<Expr> _subExpressions;

   public RelExpr(String name) {
      _subExpressions = new ArrayList<Expr>();
      _subExpressions.add(new IdExpr(name));
      _printer = new CollapsedComplexExprPrinter(this);
   }

   public void addArgument(IntExpr arg) {
      _subExpressions.add(arg);
   }

   @Override
   public List<Expr> getSubExpressions() {
      return _subExpressions;
   }

}
