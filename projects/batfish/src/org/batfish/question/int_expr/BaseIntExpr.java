package org.batfish.question.int_expr;

import org.batfish.question.Environment;

public abstract class BaseIntExpr implements IntExpr {

   public static final String print(IntExpr expr, Environment environment) {
      return expr.evaluate(environment).toString();

   }

   @Override
   public final String print(Environment environment) {
      return print(this, environment);
   }

}
