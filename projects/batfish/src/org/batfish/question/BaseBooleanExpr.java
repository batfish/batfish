package org.batfish.question;

public abstract class BaseBooleanExpr implements BooleanExpr {

   @Override
   public abstract boolean evaluate(Environment env);

   @Override
   public final String print(Environment environment) {
      return Boolean.toString(evaluate(environment));
   }

}
