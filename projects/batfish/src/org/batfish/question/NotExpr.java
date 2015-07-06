package org.batfish.question;

public class NotExpr implements BooleanExpr {

   private BooleanExpr _argument;

   public NotExpr(BooleanExpr argument) {
      _argument = argument;
   }

   @Override
   public boolean evaluate(Environment environment) {
      return !_argument.evaluate(environment);
   }

}
