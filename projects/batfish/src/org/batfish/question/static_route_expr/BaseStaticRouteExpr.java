package org.batfish.question.static_route_expr;

import org.batfish.question.Environment;

public abstract class BaseStaticRouteExpr implements StaticRouteExpr {

   public static String print(StaticRouteExpr staticRouteExpr,
         Environment environment) {
      return staticRouteExpr.evaluate(environment).toString();
   }

   @Override
   public String print(Environment environment) {
      return print(this, environment);
   }

}
