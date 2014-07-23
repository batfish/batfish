package batfish.z3.node;

import java.util.ArrayList;
import java.util.List;

public class ListExpr extends Expr implements ComplexExpr {

   private List<Expr> _subExpressions;

   public ListExpr() {
      _subExpressions = new ArrayList<Expr>();
   }

   public void addSubExpression(Expr expr) {
      _subExpressions.add(expr);
      _printer = new CollapsedComplexExprPrinter(this);
   }

   @Override
   public List<Expr> getSubExpressions() {
      return _subExpressions;
   }
}
