package org.batfish.question.static_route_expr;

import org.batfish.datamodel.StaticRoute;
import org.batfish.question.Environment;
import org.batfish.question.Expr;

public interface StaticRouteExpr extends Expr {

   @Override
   public StaticRoute evaluate(Environment environment);

}
