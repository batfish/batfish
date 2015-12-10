package org.batfish.question.string_expr.route_filter;

import org.batfish.common.BatfishException;
import org.batfish.question.Environment;
import org.batfish.question.string_expr.BaseStringExpr;
import org.batfish.question.string_expr.StringExpr;
import org.batfish.representation.RouteFilterList;

public enum RouteFilterStringExpr implements StringExpr {
   NAME;

   @Override
   public String evaluate(Environment environment) {
      RouteFilterList routeFilter = environment.getRouteFilter();
      switch (this) {

      case NAME:
         return routeFilter.getName();

      default:
         throw new BatfishException("invalid RouteFilterStringExpr");
      }
   }

   @Override
   public String print(Environment environment) {
      return BaseStringExpr.print(this, environment);
   }

}
