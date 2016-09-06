package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Not extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private BooleanExpr _expr;

   @JsonCreator
   public Not() {
   }

   public Not(BooleanExpr expr) {
      _expr = expr;
   }

   @Override
   public Result evaluate(Environment environment,
         AbstractRouteBuilder<?> outputRoute) {
      Result result = _expr.evaluate(environment, outputRoute);
      if (!result.getExit()) {
         result.setBooleanValue(!result.getBooleanValue());
      }
      return result;
   }

   public BooleanExpr getExpr() {
      return _expr;
   }

   public void setExpr(BooleanExpr expr) {
      _expr = expr;
   }

}
