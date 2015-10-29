package org.batfish.question.int_expr;

import org.batfish.question.Environment;

public class LiteralIntExpr extends BaseIntExpr {

   private final int _value;

   public LiteralIntExpr(int value) {
      _value = value;
   }

   @Override
   public Integer evaluate(Environment environment) {
      return _value;
   }

}
