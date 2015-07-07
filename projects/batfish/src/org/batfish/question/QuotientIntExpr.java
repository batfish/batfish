package org.batfish.question;

public class QuotientIntExpr extends BaseIntExpr {

   private IntExpr _dividend;

   private IntExpr _divisor;

   public QuotientIntExpr(IntExpr dividend, IntExpr divisor) {
      _dividend = dividend;
      _divisor = divisor;
   }

   @Override
   public int evaluate(Environment environment) {
      int dividend = _dividend.evaluate(environment);
      int divisor = _divisor.evaluate(environment);
      return dividend / divisor;
   }

}
