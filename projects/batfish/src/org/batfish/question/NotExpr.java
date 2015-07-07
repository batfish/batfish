package org.batfish.question;

public class NotExpr extends BaseBooleanExpr {

   private final BooleanExpr _argument;

   public NotExpr(BooleanExpr argument) {
      _argument = argument;
   }

   @Override
   public boolean evaluate(Environment environment) {
      return !_argument.evaluate(environment);
   }

}
