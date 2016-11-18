package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.routing_policy.Environment;

public class DestinationNetwork6 implements Prefix6Expr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   @Override
   public Prefix6 evaluate(Environment env) {
      return env.getOriginalRoute6().getNetwork();
   }

}
