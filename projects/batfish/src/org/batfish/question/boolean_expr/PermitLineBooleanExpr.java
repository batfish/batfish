package org.batfish.question.boolean_expr;

import org.batfish.question.Environment;
import org.batfish.question.route_filter_line_expr.RouteFilterLineExpr;
import org.batfish.representation.LineAction;
import org.batfish.representation.RouteFilterLine;

public class PermitLineBooleanExpr extends BaseBooleanExpr {

   private RouteFilterLineExpr _caller;

   public PermitLineBooleanExpr(RouteFilterLineExpr caller) {
      _caller = caller;
   }

   @Override
   public Boolean evaluate(Environment environment) {
      RouteFilterLine caller = _caller.evaluate(environment);
      return caller.getAction() == LineAction.ACCEPT;
   }

}
