package org.batfish.question.int_expr;

import org.batfish.question.Environment;

public class QuotientIntExpr extends BaseIntExpr {

   private IntExpr _dividend;

   private IntExpr _divisor;

   public QuotientIntExpr(IntExpr dividend, IntExpr divisor) {
      _dividend = dividend;
      _divisor = divisor;
   }

   @Override
   public Integer evaluate(Environment environment) {
      int dividend = _dividend.evaluate(environment);
      int divisor = _divisor.evaluate(environment);
      return dividend / divisor;
   }

}
