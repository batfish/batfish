package org.batfish.question.boolean_expr.route_filter_line;

import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.question.Environment;
import org.batfish.question.route_filter_line_expr.RouteFilterLineExpr;

public class PermitLineBooleanExpr extends RouteFilterLineBooleanExpr {

   public PermitLineBooleanExpr(RouteFilterLineExpr caller) {
      super(caller);
   }

   @Override
   public Boolean evaluate(Environment environment) {
      RouteFilterLine caller = _caller.evaluate(environment);
      return caller.getAction() == LineAction.ACCEPT;
   }

}
