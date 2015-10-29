package org.batfish.question.boolean_expr;

import org.batfish.question.Environment;

public abstract class BaseBooleanExpr implements BooleanExpr {

   public static final String print(BooleanExpr expr, Environment environment) {
      return expr.evaluate(environment).toString();
   }

   @Override
   public final String print(Environment environment) {
      return print(this, environment);
   }

}
