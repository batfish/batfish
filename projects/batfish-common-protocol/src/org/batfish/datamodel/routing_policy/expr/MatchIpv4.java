package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public class MatchIpv4 extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public MatchIpv4() {
   }

   @Override
   public Result evaluate(Environment environment) {
      boolean match = environment.getOriginalRoute() != null;
      Result result = new Result();
      result.setBooleanValue(match);
      return result;
   }

}
