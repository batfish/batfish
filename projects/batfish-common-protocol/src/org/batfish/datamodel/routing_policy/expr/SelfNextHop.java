package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment;

public class SelfNextHop implements NextHopExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   @Override
   public Ip getNextHopIp(Environment environment) {
      // TODO: make work for dynamic sessions
      return environment.getConfiguration().getBgpProcess().getNeighbors().get(
            new Prefix(environment.getPeerAddress(), Prefix.MAX_PREFIX_LENGTH))
            .getLocalIp();
   }

}
