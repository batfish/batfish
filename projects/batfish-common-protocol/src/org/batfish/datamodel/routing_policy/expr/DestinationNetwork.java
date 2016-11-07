package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment;

public class DestinationNetwork implements PrefixExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   @Override
   public Prefix evaluate(Environment env) {
      return env.getOriginalRoute().getNetwork();
   }

}
