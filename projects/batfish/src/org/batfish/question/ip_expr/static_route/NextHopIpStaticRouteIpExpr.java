package org.batfish.question.ip_expr.static_route;

import org.batfish.common.datamodel.Ip;
import org.batfish.question.Environment;
import org.batfish.question.static_route_expr.StaticRouteExpr;
import org.batfish.representation.StaticRoute;

public final class NextHopIpStaticRouteIpExpr extends StaticRouteIpExpr {

   public NextHopIpStaticRouteIpExpr(StaticRouteExpr caller) {
      super(caller);
   }

   @Override
   public Ip evaluate(Environment environment) {
      StaticRoute staticRoute = _caller.evaluate(environment);
      return staticRoute.getNextHopIp();
   }

}
