package org.batfish.question.string_set_expr;

import org.batfish.question.Environment;

public abstract class BaseStringSetExpr implements StringSetExpr {

   public static String print(StringSetExpr expr, Environment environment) {
      return expr.evaluate(environment).toString();
   }

   @Override
   public final String print(Environment environment) {
      return print(this, environment);
   }

}
