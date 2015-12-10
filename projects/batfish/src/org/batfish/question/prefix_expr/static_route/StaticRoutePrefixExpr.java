package org.batfish.question.prefix_expr.static_route;

import org.batfish.question.prefix_expr.BasePrefixExpr;
import org.batfish.question.static_route_expr.StaticRouteExpr;

public abstract class StaticRoutePrefixExpr extends BasePrefixExpr {

   protected final StaticRouteExpr _caller;

   public StaticRoutePrefixExpr(StaticRouteExpr caller) {
      _caller = caller;
   }
}
