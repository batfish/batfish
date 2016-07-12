package org.batfish.representation.cisco;

import java.util.Collections;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.routing_policy.expr.NextHopExpr;
import org.batfish.datamodel.routing_policy.expr.NextHopIp6;
import org.batfish.main.Warnings;

public class RoutePolicyNextHopIP6 extends RoutePolicyNextHop {

   private static final long serialVersionUID = 1L;

   private Ip6 _address;

   public RoutePolicyNextHopIP6(Ip6 address) {
      _address = address;
   }

   public Ip6 getAddress() {
      return _address;
   }

   @Override
   public NextHopExpr toNextHopExpr(CiscoConfiguration cc, Configuration c,
         Warnings w) {
      return new NextHopIp6(Collections.singletonList(_address));
   }

}
