package org.batfish.question.prefix_expr.static_route;

import org.batfish.common.datamodel.Prefix;
import org.batfish.question.Environment;
import org.batfish.question.static_route_expr.StaticRouteExpr;
import org.batfish.representation.StaticRoute;

public final class PrefixStaticRoutePrefixExpr extends StaticRoutePrefixExpr {

   public PrefixStaticRoutePrefixExpr(StaticRouteExpr caller) {
      super(caller);
   }

   @Override
   public Prefix evaluate(Environment environment) {
      StaticRoute staticRoute = _caller.evaluate(environment);
      return staticRoute.getPrefix();
   }

}
