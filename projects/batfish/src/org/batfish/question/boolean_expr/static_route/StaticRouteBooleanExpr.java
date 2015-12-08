package org.batfish.question.boolean_expr.static_route;

import org.batfish.question.boolean_expr.BaseBooleanExpr;
import org.batfish.question.static_route_expr.StaticRouteExpr;

public abstract class StaticRouteBooleanExpr extends BaseBooleanExpr {

   protected final StaticRouteExpr _caller;

   public StaticRouteBooleanExpr(StaticRouteExpr caller) {
      _caller = caller;
   }

}
