package org.batfish.question.route_filter_line_expr;

import org.batfish.datamodel.RouteFilterLine;
import org.batfish.question.Environment;

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
