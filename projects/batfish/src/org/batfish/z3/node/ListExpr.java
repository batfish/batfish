package org.batfish.z3.node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ListExpr extends Expr implements ComplexExpr {

   protected final List<Expr> _subExpressions;

   public ListExpr() {
      _subExpressions = new ArrayList<Expr>();
   }

   public void addSubExpression(Expr expr) {
      _subExpressions.add(expr);
   }

   @Override
   public List<Expr> getSubExpressions() {
      return _subExpressions;
   }

   @Override
   public Set<String> getVariables() {
      Set<String> variables = new HashSet<String>();
      for (Expr subExpression : _subExpressions) {
         variables.addAll(subExpression.getVariables());
      }
      return variables;
   }

}
