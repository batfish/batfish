package org.batfish.question.route_filter_line_expr;

import org.batfish.datamodel.RouteFilterLine;
import org.batfish.question.Environment;
import org.batfish.question.Expr;

public interface RouteFilterLineExpr extends Expr {

   @Override
   RouteFilterLine evaluate(Environment environment);

}
