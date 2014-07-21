package batfish.z3.node;

import java.util.ArrayList;
import java.util.List;

public abstract class ComplexExpr extends Expr {

   protected List<Expr> _subExpressions;

   public ComplexExpr() {
      _subExpressions = new ArrayList<Expr>();
   }

   public void addSubExpression(Expr subExpression) {
      _subExpressions.add(subExpression);
   }

}
