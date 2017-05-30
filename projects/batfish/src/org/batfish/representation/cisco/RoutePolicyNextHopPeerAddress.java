package org.batfish.representation.cisco;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.NextHopExpr;
import org.batfish.datamodel.routing_policy.expr.PeerAddressNextHop;
import org.batfish.common.Warnings;

public class RoutePolicyNextHopPeerAddress extends RoutePolicyNextHop {

   private static final long serialVersionUID = 1L;

   @Override
   public NextHopExpr toNextHopExpr(CiscoConfiguration cc, Configuration c,
         Warnings w) {
      return new PeerAddressNextHop();
   }

}
