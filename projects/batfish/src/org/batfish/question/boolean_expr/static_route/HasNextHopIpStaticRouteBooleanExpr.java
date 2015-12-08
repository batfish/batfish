package org.batfish.question.boolean_expr.static_route;

import org.batfish.question.Environment;
import org.batfish.question.static_route_expr.StaticRouteExpr;
import org.batfish.representation.StaticRoute;

public class HasNextHopIpStaticRouteBooleanExpr extends StaticRouteBooleanExpr {

   public HasNextHopIpStaticRouteBooleanExpr(StaticRouteExpr caller) {
      super(caller);
   }

   @Override
   public Boolean evaluate(Environment environment) {
      StaticRoute staticRoute = _caller.evaluate(environment);
      return staticRoute.getNextHopIp() != null;
   }

}
