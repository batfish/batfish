package org.batfish.question.int_expr;

import org.batfish.question.Environment;

public class ProductIntExpr extends BaseIntExpr {

   private final IntExpr _multiplicand1;

   private final IntExpr _multiplicand2;

   public ProductIntExpr(IntExpr multiplicand1, IntExpr multiplicand2) {
      _multiplicand1 = multiplicand1;
      _multiplicand2 = multiplicand2;
   }

   @Override
   public Integer evaluate(Environment environment) {
      int m1 = _multiplicand1.evaluate(environment);
      int m2 = _multiplicand2.evaluate(environment);
      return m1 * m2;
   }

}
