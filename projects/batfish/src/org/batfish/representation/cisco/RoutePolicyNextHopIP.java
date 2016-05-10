package org.batfish.representation.cisco;

import org.batfish.common.datamodel.Ip;

public class RoutePolicyNextHopIP extends RoutePolicyNextHop {

   private static final long serialVersionUID = 1L;
   private Ip _address;

   public RoutePolicyNextHopIP(Ip address) {
      _address = address;
   }

   public Ip getAddress() {
      return _address;
   }

   @Override
   public RoutePolicyNextHopType getNextHopType() {
      return RoutePolicyNextHopType.IP;
   }

}
