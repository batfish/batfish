package org.batfish.question.prefix_space_expr;

import org.batfish.question.Environment;

public abstract class BasePrefixSpaceExpr implements PrefixSpaceExpr {

   public static final String print(PrefixSpaceExpr expr,
         Environment environment) {
      return expr.evaluate(environment).toString();
   }

   @Override
   public final String print(Environment environment) {
      return print(this, environment);
   }

}
