package org.batfish.question.route_filter_line_expr;

import org.batfish.question.Environment;
import org.batfish.question.Expr;
import org.batfish.representation.RouteFilterLine;

public interface RouteFilterLineExpr extends Expr {

   @Override
   RouteFilterLine evaluate(Environment environment);

}
