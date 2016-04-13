package org.batfish.question.string_expr.route;

import org.batfish.question.route_expr.RouteExpr;
import org.batfish.question.string_expr.BaseStringExpr;

public abstract class RouteStringExpr extends BaseStringExpr {

   protected final RouteExpr _caller;

   public RouteStringExpr(RouteExpr caller) {
      _caller = caller;
   }

}
