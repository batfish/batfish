package org.batfish.representation.cisco;

public class RoutePolicyNextHopSelf extends RoutePolicyNextHop {

   private static final long serialVersionUID = 1L;

   @Override
   public RoutePolicyNextHopType getNextHopType() {
      return RoutePolicyNextHopType.SELF;
   }

}
