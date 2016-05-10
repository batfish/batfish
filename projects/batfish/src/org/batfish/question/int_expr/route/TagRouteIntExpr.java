package org.batfish.question.int_expr.route;

import org.batfish.datamodel.PrecomputedRoute;
import org.batfish.question.Environment;
import org.batfish.question.route_expr.RouteExpr;

public final class TagRouteIntExpr extends RouteIntExpr {

   public TagRouteIntExpr(RouteExpr caller) {
      super(caller);
   }

   @Override
   public Integer evaluate(Environment environment) {
      PrecomputedRoute caller = _caller.evaluate(environment);
      return caller.getTag();
   }

}
