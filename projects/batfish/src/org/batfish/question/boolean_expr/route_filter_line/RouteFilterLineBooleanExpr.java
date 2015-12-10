package org.batfish.question.boolean_expr.route_filter_line;

import org.batfish.question.boolean_expr.BaseBooleanExpr;
import org.batfish.question.route_filter_line_expr.RouteFilterLineExpr;

public abstract class RouteFilterLineBooleanExpr extends BaseBooleanExpr {

   protected final RouteFilterLineExpr _caller;

   public RouteFilterLineBooleanExpr(RouteFilterLineExpr caller) {
      _caller = caller;
   }

}
