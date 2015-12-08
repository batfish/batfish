package org.batfish.question.route_filter_line_expr;

import org.batfish.question.Environment;
import org.batfish.representation.RouteFilterLine;

public final class VarRouteFilterLineExpr extends BaseRouteFilterLineExpr {

   private final String _var;

   public VarRouteFilterLineExpr(String var) {
      _var = var;
   }

   @Override
   public RouteFilterLine evaluate(Environment environment) {
      return environment.getRouteFilterLines().get(_var);
   }

}
