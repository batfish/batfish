package org.batfish.representation.cisco;

public class RoutePolicySetNextHop extends RoutePolicySetStatement {

   private static final long serialVersionUID = 1L;

   private boolean _destination_vrf;
   private RoutePolicyNextHop _nextHop;

   public RoutePolicySetNextHop(RoutePolicyNextHop nextHop,
         boolean destination_vrf) {
      _nextHop = nextHop;
      _destination_vrf = destination_vrf;
   }

   public boolean getDestination_VRF() {
      return _destination_vrf;
   }

   public RoutePolicyNextHop getNextHop() {
      return _nextHop;
   }

   @Override
   public RoutePolicySetType getSetType() {
      return RoutePolicySetType.NEXT_HOP;
   }

}
