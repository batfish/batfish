package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.Environment;

public class NextHopPeerAddress implements NextHopExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   @Override
   public Ip getNextHopIp(Environment environment) {
      return environment.getPeerAddress();
   }

}
