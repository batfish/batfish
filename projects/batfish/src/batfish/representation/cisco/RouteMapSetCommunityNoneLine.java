package batfish.representation.cisco;

import batfish.representation.Configuration;
import batfish.representation.PolicyMapSetCommunityNoneLine;
import batfish.representation.PolicyMapSetLine;

public class RouteMapSetCommunityNoneLine extends RouteMapSetLine {

   @Override
   public PolicyMapSetLine toPolicyMapSetLine(Configuration c) {
      return new PolicyMapSetCommunityNoneLine();
   }

}
