package org.batfish.question.map_expr;

import org.batfish.question.Environment;

public abstract class BaseMapExpr implements MapExpr {

   public static String print(MapExpr expr, Environment environment) {
      return expr.evaluate(environment).toString();
   }

   @Override
   public final String print(Environment environment) {
      return print(this, environment);
   }

}
