package org.batfish.question.prefix_expr.route;

import org.batfish.question.prefix_expr.BasePrefixExpr;
import org.batfish.question.route_expr.RouteExpr;

public abstract class RoutePrefixExpr extends BasePrefixExpr {

   protected final RouteExpr _caller;

   public RoutePrefixExpr(RouteExpr caller) {
      _caller = caller;
   }

}
