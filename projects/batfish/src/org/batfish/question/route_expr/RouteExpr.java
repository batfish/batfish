package org.batfish.question.route_expr;

import org.batfish.datamodel.PrecomputedRoute;
import org.batfish.question.Environment;
import org.batfish.question.Expr;

public interface RouteExpr extends Expr {

   @Override
   PrecomputedRoute evaluate(Environment environment);

}
