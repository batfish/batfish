package org.batfish.question;

public class DifferenceIntExpr extends BaseIntExpr {

   private final IntExpr _minuend;

   private final IntExpr _subtrahend;

   public DifferenceIntExpr(IntExpr subtrahend, IntExpr minuend) {
      _subtrahend = subtrahend;
      _minuend = minuend;
   }

   @Override
   public int evaluate(Environment environment) {
      int subtrahend = _subtrahend.evaluate(environment);
      int minuend = _minuend.evaluate(environment);
      return subtrahend - minuend;
   }

}
