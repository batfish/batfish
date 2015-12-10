package org.batfish.question.int_expr.static_route;

import org.batfish.question.Environment;
import org.batfish.question.static_route_expr.StaticRouteExpr;
import org.batfish.representation.StaticRoute;

public final class AdministrativeCostStaticRouteIntExpr extends
      StaticRouteIntExpr {

   public AdministrativeCostStaticRouteIntExpr(StaticRouteExpr caller) {
      super(caller);
   }

   @Override
   public Integer evaluate(Environment environment) {
      StaticRoute staticRoute = _caller.evaluate(environment);
      return staticRoute.getAdministrativeCost();
   }

}
