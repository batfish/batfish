package org.batfish.question.static_route_expr;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.StaticRoute;
import org.batfish.question.Environment;

public enum BaseCaseStaticRouteExpr implements StaticRouteExpr {
   STATIC_ROUTE;

   @Override
   public StaticRoute evaluate(Environment environment) {
      switch (this) {

      case STATIC_ROUTE:
         return environment.getStaticRoute();

      default:
         throw new BatfishException("Invalid "
               + this.getClass().getSimpleName());

      }
   }

   @Override
   public String print(Environment environment) {
      return BaseStaticRouteExpr.print(this, environment);
   }

}
