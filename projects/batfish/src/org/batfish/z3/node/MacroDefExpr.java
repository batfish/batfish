package org.batfish.z3.node;

import java.util.ArrayList;
import java.util.List;

public class MacroDefExpr extends Expr implements ComplexExpr {

   private BooleanExpr _expression;
   private String _macro;
   private List<Expr> _subExpressions;

   public MacroDefExpr(String macro, BooleanExpr b) {
      _macro = macro;
      _expression = b;
      _subExpressions = new ArrayList<Expr>();
      _printer = new ExpandedComplexExprPrinter(this);
      refreshSubexpressions();
   }

   @Override
   public List<Expr> getSubExpressions() {
      return _subExpressions;
   }

   private void refreshSubexpressions() {
      _subExpressions.clear();
      _subExpressions.add(new IdExpr(_macro));
      _subExpressions.add(_expression);
   }

}
