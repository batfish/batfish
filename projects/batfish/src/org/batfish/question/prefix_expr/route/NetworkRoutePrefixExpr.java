package org.batfish.question.prefix_expr.route;

import org.batfish.question.Environment;
import org.batfish.question.route_expr.RouteExpr;
import org.batfish.representation.PrecomputedRoute;
import org.batfish.representation.Prefix;

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
