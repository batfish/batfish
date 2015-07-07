package org.batfish.question;

public abstract class BaseIntExpr implements IntExpr {

   @Override
   public abstract int evaluate(Environment environment);

   @Override
   public final String print(Environment environment) {
      return Integer.toString(evaluate(environment));
   }

}
