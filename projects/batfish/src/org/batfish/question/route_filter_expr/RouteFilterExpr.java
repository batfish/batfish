package org.batfish.question.route_filter_expr;

import org.batfish.datamodel.RouteFilterList;
import org.batfish.question.Environment;
import org.batfish.question.Expr;

public interface RouteFilterExpr extends Expr {

   @Override
   RouteFilterList evaluate(Environment environment);

}
