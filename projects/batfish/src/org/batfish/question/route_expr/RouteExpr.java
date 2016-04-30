package org.batfish.question.route_expr;

import org.batfish.question.Environment;
import org.batfish.question.Expr;
import org.batfish.representation.PrecomputedRoute;

public interface RouteExpr extends Expr {

   @Override
   PrecomputedRoute evaluate(Environment environment);

}
