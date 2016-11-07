package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.Environment;

public class NextHopIp implements IpExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   @Override
   public Ip evaluate(Environment env) {
      return env.getOriginalRoute().getNextHopIp();
   }

}
