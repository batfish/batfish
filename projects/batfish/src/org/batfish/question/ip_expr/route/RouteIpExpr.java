package org.batfish.question.ip_expr.route;

import org.batfish.question.ip_expr.BaseIpExpr;
import org.batfish.question.route_expr.RouteExpr;

public abstract class RouteIpExpr extends BaseIpExpr {

   protected final RouteExpr _caller;

   public RouteIpExpr(RouteExpr caller) {
      _caller = caller;
   }

}
