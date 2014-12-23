package batfish.representation.cisco;

import batfish.representation.Configuration;
import batfish.representation.PolicyMapSetCommunityNoneLine;
import batfish.representation.PolicyMapSetLine;

public class RouteMapSetCommunityNoneLine extends RouteMapSetLine {

   private static final long serialVersionUID = 1L;

   @Override
   public RouteMapSetType getType() {
      return RouteMapSetType.COMMUNITY_NONE;
   }

   @Override
   public PolicyMapSetLine toPolicyMapSetLine(Configuration c) {
      return new PolicyMapSetCommunityNoneLine();
   }

}
