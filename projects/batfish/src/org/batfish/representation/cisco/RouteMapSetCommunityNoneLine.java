package org.batfish.representation.cisco;

import org.batfish.main.Warnings;
import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapSetCommunityNoneLine;
import org.batfish.representation.PolicyMapSetLine;

public class RouteMapSetCommunityNoneLine extends RouteMapSetLine {

   private static final long serialVersionUID = 1L;

   @Override
   public RouteMapSetType getType() {
      return RouteMapSetType.COMMUNITY_NONE;
   }

   @Override
   public PolicyMapSetLine toPolicyMapSetLine(CiscoConfiguration v, Configuration c, Warnings w) {
      return new PolicyMapSetCommunityNoneLine();
   }

}
