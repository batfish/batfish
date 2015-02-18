package org.batfish.z3.node;

import java.util.Collections;
import java.util.List;

public class GetModelExpr extends Statement implements ComplexExpr {

   public static GetModelExpr INSTANCE = new GetModelExpr();

   private List<Expr> _subExpressions;

   private GetModelExpr() {
      _subExpressions = Collections
            .<Expr> singletonList(new IdExpr("get-model"));
      _printer = new CollapsedComplexExprPrinter(this);
   }

   @Override
   public List<Expr> getSubExpressions() {
      return _subExpressions;
   }

}
