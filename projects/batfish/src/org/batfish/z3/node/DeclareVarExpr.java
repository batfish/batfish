package org.batfish.z3.node;

import java.util.ArrayList;
import java.util.List;

public class DeclareVarExpr extends Statement implements ComplexExpr {

   private List<Expr> _subExpressions;

   public DeclareVarExpr(String name, int size) {
      _subExpressions = new ArrayList<Expr>();
      _subExpressions.add(new IdExpr("declare-var"));
      _subExpressions.add(new IdExpr(name));
      _subExpressions.add(new BitVecExpr(size));
      _printer = new CollapsedComplexExprPrinter(this);
   }

   @Override
   public List<Expr> getSubExpressions() {
      return _subExpressions;
   }

}
