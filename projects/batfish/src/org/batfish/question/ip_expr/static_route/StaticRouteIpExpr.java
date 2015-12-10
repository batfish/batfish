package org.batfish.question.ip_expr.static_route;

import org.batfish.question.ip_expr.BaseIpExpr;
import org.batfish.question.static_route_expr.StaticRouteExpr;

public abstract class StaticRouteIpExpr extends BaseIpExpr {

   protected final StaticRouteExpr _caller;

   public StaticRouteIpExpr(StaticRouteExpr caller) {
      _caller = caller;
   }

}
