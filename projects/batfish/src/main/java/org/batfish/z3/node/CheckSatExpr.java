package org.batfish.z3.node;

import java.util.Collections;
import java.util.List;

public class CheckSatExpr extends Statement implements ComplexExpr {

   public static CheckSatExpr INSTANCE = new CheckSatExpr();

   private List<Expr> _subExpressions;

   private CheckSatExpr() {
      _subExpressions = Collections
            .<Expr> singletonList(new IdExpr("check-sat"));
      _printer = new CollapsedComplexExprPrinter(this);
   }

   @Override
   public List<Expr> getSubExpressions() {
      return _subExpressions;
   }

}
