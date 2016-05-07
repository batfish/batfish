package org.batfish.representation.cisco;

import java.io.Serializable;

public class RoutePolicyNextHopSelf extends RoutePolicyNextHop {

   private static final long serialVersionUID = 1L;

   public RoutePolicyNextHopType getNextHopType() { 
   	return RoutePolicyNextHopType.SELF; 
   }

}
