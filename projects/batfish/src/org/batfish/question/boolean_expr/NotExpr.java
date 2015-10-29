package org.batfish.question.boolean_expr;

import org.batfish.question.Environment;

public class NotExpr extends BaseBooleanExpr {

   private final BooleanExpr _argument;

   public NotExpr(BooleanExpr argument) {
      _argument = argument;
   }

   @Override
   public Boolean evaluate(Environment environment) {
      return !_argument.evaluate(environment);
   }

}
