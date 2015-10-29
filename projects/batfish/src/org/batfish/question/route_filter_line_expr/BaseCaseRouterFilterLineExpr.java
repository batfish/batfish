package org.batfish.question.route_filter_line_expr;

import org.batfish.common.BatfishException;
import org.batfish.question.Environment;
import org.batfish.representation.RouteFilterLine;

public enum BaseCaseRouterFilterLineExpr implements RouteFilterLineExpr {
   LINE;

   @Override
   public RouteFilterLine evaluate(Environment environment) {
      RouteFilterLine line = environment.getRouteFilterLine();
      switch (this) {

      case LINE:
         return line;

      default:
         throw new BatfishException("invalid RouteFilterLineExpr");
      }
   }

   @Override
   public String print(Environment environment) {
      return BaseRouteFilterLineExpr.print(this, environment);
   }

}
