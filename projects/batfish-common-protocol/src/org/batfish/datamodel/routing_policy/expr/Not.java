package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Route;
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
   public Result evaluate(Environment environment, Route route) {
      Result result = _expr.evaluate(environment, route);
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
