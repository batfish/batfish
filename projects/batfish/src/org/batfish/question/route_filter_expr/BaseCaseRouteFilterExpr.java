package org.batfish.question.route_filter_expr;

import org.batfish.common.BatfishException;
import org.batfish.question.Environment;
import org.batfish.representation.RouteFilterList;

public enum BaseCaseRouteFilterExpr implements RouteFilterExpr {
   ROUTE_FILTER;

   @Override
   public RouteFilterList evaluate(Environment environment) {
      switch (this) {
      case ROUTE_FILTER:
         return environment.getRouteFilter();

      default:
         throw new BatfishException("invalid BaseCaseRouteFilterExpr");

      }
   }

   @Override
   public String print(Environment environment) {
      return BaseRouteFilterExpr.print(this, environment);
   }

}
