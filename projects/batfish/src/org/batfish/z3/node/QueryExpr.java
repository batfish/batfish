package org.batfish.z3.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class QueryExpr extends Statement implements ComplexExpr {

   private BooleanExpr _subExpression;
   private List<Expr> _subExpressions;

   public QueryExpr(BooleanExpr expr) {
      _subExpression = expr;
      init();
   }

   @Override
   public Set<String> getRelations() {
      return _subExpression.getRelations();
   }

   @Override
   public List<Expr> getSubExpressions() {
      return _subExpressions;
   }

   @Override
   public Set<String> getVariables() {
      return _subExpression.getVariables();
   }

   private void init() {
      _subExpressions = new ArrayList<Expr>();
      _subExpressions.add(new IdExpr("query"));
      _subExpressions.add(_subExpression);
      _printer = new CollapsedComplexExprPrinter(this);
   }

}
