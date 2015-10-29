package org.batfish.question.int_expr;

import org.batfish.question.Environment;

public class SumIntExpr extends BaseIntExpr {

   private final IntExpr _addend1;

   private final IntExpr _addend2;

   public SumIntExpr(IntExpr addend1, IntExpr addend2) {
      _addend1 = addend1;
      _addend2 = addend2;
   }

   @Override
   public Integer evaluate(Environment environment) {
      int addend1 = _addend1.evaluate(environment);
      int addend2 = _addend2.evaluate(environment);
      return addend1 + addend2;
   }

}
