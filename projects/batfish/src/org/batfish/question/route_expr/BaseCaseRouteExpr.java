package org.batfish.question.route_expr;

import org.batfish.common.BatfishException;
import org.batfish.question.Environment;
import org.batfish.representation.PrecomputedRoute;

public enum BaseCaseRouteExpr implements RouteExpr {
   ROUTE;

   @Override
   public PrecomputedRoute evaluate(Environment environment) {
      switch (this) {
      case ROUTE:
         return environment.getRoute();

      default:
         throw new BatfishException("invalid BaseCaseRouteExpr");

      }
   }

   @Override
   public String print(Environment environment) {
      return BaseRouteExpr.print(this, environment);
   }

}
