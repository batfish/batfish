package org.batfish.representation.cisco;

import org.batfish.representation.Ip6;

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
   public RoutePolicyNextHopType getNextHopType() {
      return RoutePolicyNextHopType.IP;
   }

}
