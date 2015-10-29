package org.batfish.question.route_filter_line_expr;

import org.batfish.question.Environment;

public abstract class BaseRouteFilterLineExpr implements RouteFilterLineExpr {

   public static String print(RouteFilterLineExpr expr, Environment environment) {
      return expr.evaluate(environment).toString();
   }

   @Override
   public final String print(Environment environment) {
      return print(this, environment);
   }

}
