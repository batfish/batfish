package org.batfish.question.prefix_expr.route;

import org.batfish.datamodel.PrecomputedRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.question.Environment;
import org.batfish.question.route_expr.RouteExpr;

public final class NetworkRoutePrefixExpr extends RoutePrefixExpr {

   public NetworkRoutePrefixExpr(RouteExpr caller) {
      super(caller);
   }

   @Override
   public Prefix evaluate(Environment environment) {
      PrecomputedRoute caller = _caller.evaluate(environment);
      return caller.getPrefix();
   }

}
