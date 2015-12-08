package org.batfish.question.route_filter_expr;

import org.batfish.question.Environment;
import org.batfish.representation.RouteFilterList;

public final class VarRouteFilterExpr extends BaseRouteFilterExpr {

   private final String _var;

   public VarRouteFilterExpr(String var) {
      _var = var;
   }

   @Override
   public RouteFilterList evaluate(Environment environment) {
      return environment.getRouteFilters().get(_var);
   }

}
