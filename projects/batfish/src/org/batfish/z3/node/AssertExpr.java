package org.batfish.z3.node;

import java.util.ArrayList;
import java.util.List;

public class AssertExpr extends Statement implements ComplexExpr {

   private BooleanExpr _arg;
   private List<Expr> _subExpressions;

   public AssertExpr() {
      init();
   }

   public AssertExpr(BooleanExpr arg) {
      init();
      _arg = arg;
      refreshSubExpressions();
   }

   @Override
   public List<Expr> getSubExpressions() {
      return _subExpressions;
   }

   private void init() {
      _subExpressions = new ArrayList<Expr>();
      _printer = new ExpandedComplexExprPrinter(this);
   }

   private void refreshSubExpressions() {
      _subExpressions.clear();
      _subExpressions.add(new IdExpr("assert"));
      _subExpressions.add(_arg);
   }

}
