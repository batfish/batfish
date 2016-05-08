package org.batfish.question.route_expr;

import org.batfish.common.datamodel.PrecomputedRoute;
import org.batfish.question.Environment;
import org.batfish.question.Expr;

public interface RouteExpr extends Expr {

   @Override
   PrecomputedRoute evaluate(Environment environment);

}
