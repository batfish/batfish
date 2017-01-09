package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public class MatchIpv6 extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public MatchIpv6() {
   }

   @Override
   public Result evaluate(Environment environment) {
      boolean match = environment.getOriginalRoute6() != null;
      Result result = new Result();
      result.setBooleanValue(match);
      return result;
   }

}
