package org.batfish.question.int_expr.route;

import org.batfish.question.int_expr.BaseIntExpr;
import org.batfish.question.route_expr.RouteExpr;

public abstract class RouteIntExpr extends BaseIntExpr {

   protected final RouteExpr _caller;

   public RouteIntExpr(RouteExpr caller) {
      _caller = caller;
   }

}
