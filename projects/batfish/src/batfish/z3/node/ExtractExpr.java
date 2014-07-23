package batfish.z3.node;

import java.util.ArrayList;
import java.util.List;

public class ExtractExpr extends IntExpr implements ComplexExpr {

   private List<Expr> _subExpressions;

   public ExtractExpr(String var, int low, int high) {
      _subExpressions = new ArrayList<Expr>();
      ListExpr listExpr = new ListExpr();
      listExpr.addSubExpression(new IdExpr("_"));
      listExpr.addSubExpression(new IdExpr("extract"));
      listExpr.addSubExpression(new IdExpr(Integer.toString(high)));
      listExpr.addSubExpression(new IdExpr(Integer.toString(low)));
      _subExpressions.add(listExpr);
      _subExpressions.add(new IdExpr(var));
      _printer = new CollapsedComplexExprPrinter(this);
   }
   
   @Override
   public List<Expr> getSubExpressions() {
      return _subExpressions;
   }

}
