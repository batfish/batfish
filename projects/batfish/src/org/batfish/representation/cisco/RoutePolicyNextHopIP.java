package org.batfish.representation.cisco;

import java.io.Serializable;

import org.batfish.representation.Ip;

public class RoutePolicyNextHopIP extends RoutePolicyNextHop {

   private static final long serialVersionUID = 1L;
   private Ip _address;

   public RoutePolicyNextHopIP(Ip address) { _address = address; }

   public RoutePolicyNextHopType getNextHopType() { 
   	return RoutePolicyNextHopType.IP; 
   }

   public Ip getAddress() { return _address; }

}
