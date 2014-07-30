package batfish.z3.node;

import java.util.ArrayList;
import java.util.List;

public class ListExpr extends Expr implements ComplexExpr {

   private List<Expr> _subExpressions;

   public ListExpr() {
      _subExpressions = new ArrayList<Expr>();
      _printer = new CollapsedComplexExprPrinter(this);
   }

   public void addSubExpression(Expr expr) {
      _subExpressions.add(expr);
   }

   @Override
   public List<Expr> getSubExpressions() {
      return _subExpressions;
   }
}
