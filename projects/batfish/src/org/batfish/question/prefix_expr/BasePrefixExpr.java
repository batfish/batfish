package org.batfish.question.prefix_expr;

import org.batfish.question.Environment;

public abstract class BasePrefixExpr implements PrefixExpr {

   public static String print(PrefixExpr expr, Environment environment) {
      return expr.evaluate(environment).toString();
   }

   @Override
   public final String print(Environment environment) {
      return print(this, environment);
   }

}
