package org.batfish.z3.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RelExpr extends BooleanExpr implements ComplexExpr {

   private String _name;
   private List<Expr> _subExpressions;

   public RelExpr(String name) {
      _name = name;
      _subExpressions = new ArrayList<Expr>();
      _subExpressions.add(new IdExpr(name));
      _printer = new CollapsedComplexExprPrinter(this);
   }

   public void addArgument(IntExpr arg) {
      _subExpressions.add(arg);
   }

   @Override
   public Set<String> getRelations() {
      return Collections.singleton(_name);
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
