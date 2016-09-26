package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public class MatchAsPath extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private AsPathSetExpr _expr;

   public MatchAsPath(AsPathSetExpr expr) {
      _expr = expr;
   }

   @Override
   public Result evaluate(Environment environment) {
      boolean match = _expr.matches(environment);
      Result result = new Result();
      result.setBooleanValue(match);
      return result;
   }

   public AsPathSetExpr getExpr() {
      return _expr;
   }

   public void setExpr(AsPathSetExpr expr) {
      _expr = expr;
   }

}
