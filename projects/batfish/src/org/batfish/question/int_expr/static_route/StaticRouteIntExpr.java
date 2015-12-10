package org.batfish.question.int_expr.static_route;

import org.batfish.question.int_expr.BaseIntExpr;
import org.batfish.question.static_route_expr.StaticRouteExpr;

public abstract class StaticRouteIntExpr extends BaseIntExpr {

   protected final StaticRouteExpr _caller;

   public StaticRouteIntExpr(StaticRouteExpr caller) {
      _caller = caller;
   }

}
