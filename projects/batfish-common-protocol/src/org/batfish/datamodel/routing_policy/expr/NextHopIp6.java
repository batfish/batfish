package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.routing_policy.Environment;

public class NextHopIp6 implements Ip6Expr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   @Override
   public Ip6 evaluate(Environment env) {
      return env.getOriginalRoute6().getNextHopIp();
   }

}
