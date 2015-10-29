package org.batfish.question.route_filter_expr;

import org.batfish.question.Environment;

public abstract class BaseRouteFilterExpr implements RouteFilterExpr {

   public static String print(RouteFilterExpr expr, Environment environment) {
      return expr.evaluate(environment).toString();
   }

   @Override
   public final String print(Environment environment) {
      return print(this, environment);
   }

}
