package org.batfish.z3.node;

import java.util.ArrayList;
import java.util.List;

public class BitVecExpr extends TypeExpr implements ComplexExpr {

   private List<Expr> _subExpressions;

   public BitVecExpr(int size) {
      _subExpressions = new ArrayList<Expr>();
      _subExpressions.add(new IdExpr("_"));
      _subExpressions.add(new IdExpr("BitVec"));
      _subExpressions.add(new IdExpr(Integer.toString(size)));
      _printer = new CollapsedComplexExprPrinter(this);
   }

   @Override
   public List<Expr> getSubExpressions() {
      return _subExpressions;
   }
}
