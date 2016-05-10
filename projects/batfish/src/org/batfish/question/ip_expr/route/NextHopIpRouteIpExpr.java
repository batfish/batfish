package org.batfish.question.ip_expr.route;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.PrecomputedRoute;
import org.batfish.question.Environment;
import org.batfish.question.route_expr.RouteExpr;

public class NextHopIpRouteIpExpr extends RouteIpExpr {

   public NextHopIpRouteIpExpr(RouteExpr caller) {
      super(caller);
   }

   @Override
   public Ip evaluate(Environment environment) {
      PrecomputedRoute caller = _caller.evaluate(environment);
      return caller.getNextHopIp();
   }

}
