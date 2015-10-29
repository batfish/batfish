package org.batfish.question.route_filter_expr;

import org.batfish.question.Environment;
import org.batfish.question.Expr;
import org.batfish.representation.RouteFilterList;

public interface RouteFilterExpr extends Expr {

   @Override
   RouteFilterList evaluate(Environment environment);

}
