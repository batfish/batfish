package org.batfish.question.string_expr.route;

import org.batfish.question.Environment;
import org.batfish.question.route_expr.RouteExpr;
import org.batfish.representation.PrecomputedRoute;

public final class NextHopRouteStringExpr extends RouteStringExpr {

   public NextHopRouteStringExpr(RouteExpr caller) {
      super(caller);
   }

   @Override
   public String evaluate(Environment environment) {
      PrecomputedRoute caller = _caller.evaluate(environment);
      return caller.getNextHop();
   }

}
