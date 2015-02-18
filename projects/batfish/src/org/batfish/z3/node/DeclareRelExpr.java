package org.batfish.z3.node;

import java.util.ArrayList;
import java.util.List;

public class DeclareRelExpr extends Statement implements ComplexExpr {

   private List<Expr> _subExpressions;

   public DeclareRelExpr(String name, List<Integer> sizes) {
      _subExpressions = new ArrayList<Expr>();
      _subExpressions.add(new IdExpr("declare-rel"));
      _subExpressions.add(new IdExpr(name));
      ListExpr listExpression = new CollapsedListExpr();
      _subExpressions.add(listExpression);
      for (int size : sizes) {
         listExpression.addSubExpression(new BitVecExpr(size));
      }
      _printer = new CollapsedComplexExprPrinter(this);
   }

   @Override
   public List<Expr> getSubExpressions() {
      return _subExpressions;
   }

}
