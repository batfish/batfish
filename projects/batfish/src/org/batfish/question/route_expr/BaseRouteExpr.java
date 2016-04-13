package org.batfish.question.route_expr;

import org.batfish.question.Environment;

public abstract class BaseRouteExpr implements RouteExpr {

   public static String print(RouteExpr expr, Environment environment) {
      return expr.evaluate(environment).toString();
   }

   @Override
   public final String print(Environment environment) {
      return print(this, environment);
   }

}
