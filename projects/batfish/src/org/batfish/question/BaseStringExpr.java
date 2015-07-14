package org.batfish.question;

public abstract class BaseStringExpr implements StringExpr {

   @Override
   public String print(Environment environment) {
      return evaluate(environment);
   }

}
