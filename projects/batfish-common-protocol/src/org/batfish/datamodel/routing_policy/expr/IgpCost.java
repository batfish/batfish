package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;

public class IgpCost implements IntExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   @Override
   public int evaluate(Environment environment) {
      return environment.getOriginalRoute().getMetric();
   }

}
