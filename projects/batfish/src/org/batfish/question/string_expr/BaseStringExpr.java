package org.batfish.question.string_expr;

import org.batfish.question.Environment;

public abstract class BaseStringExpr implements StringExpr {

   public static String print(StringExpr expr, Environment environment) {
      return expr.evaluate(environment);
   }

   @Override
   public final String print(Environment environment) {
      return print(this, environment);
   }

}
