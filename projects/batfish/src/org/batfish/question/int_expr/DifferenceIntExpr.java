package org.batfish.question.int_expr;

import org.batfish.question.Environment;

public class DifferenceIntExpr extends BaseIntExpr {

   private final IntExpr _minuend;

   private final IntExpr _subtrahend;

   public DifferenceIntExpr(IntExpr subtrahend, IntExpr minuend) {
      _subtrahend = subtrahend;
      _minuend = minuend;
   }

   @Override
   public Integer evaluate(Environment environment) {
      int subtrahend = _subtrahend.evaluate(environment);
      int minuend = _minuend.evaluate(environment);
      return subtrahend - minuend;
   }

}
