package org.batfish.question.prefix_set_expr;

import org.batfish.question.Environment;

public abstract class BasePrefixSetExpr implements PrefixSetExpr {

   public static String print(PrefixSetExpr expr, Environment environment) {
      return expr.evaluate(environment).toString();
   }

   @Override
   public final String print(Environment environment) {
      return print(this, environment);
   }

}
