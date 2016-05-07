package org.batfish.representation.cisco;

import java.io.Serializable;

public class RoutePolicySetNextHop extends RoutePolicySetStatement {

   private static final long serialVersionUID = 1L;

   private RoutePolicyNextHop _nextHop; 
   private boolean _destination_vrf;

   public RoutePolicySetNextHop(RoutePolicyNextHop nextHop, boolean destination_vrf) {
   	_nextHop = nextHop;
   	_destination_vrf = destination_vrf;
   }


   public RoutePolicySetType getSetType() { return RoutePolicySetType.NEXT_HOP; }

   public RoutePolicyNextHop getNextHop() { return _nextHop; }

   public boolean getDestination_VRF() { return _destination_vrf; }

}
