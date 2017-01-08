package org.batfish.datamodel.routing_policy.expr;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.routing_policy.Environment;

public class DestinationNetwork6 implements Prefix6Expr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   @Override
   public Prefix6 evaluate(Environment env) {
      if (env.getOriginalRoute6() != null) {
         return env.getOriginalRoute6().getNetwork();
      }
      else if (env.getOriginalRoute() == null) {
         throw new BatfishException("No IPV4 nor IPV6 route passed as input");
      }
      else {
         return null;
      }
   }

}
