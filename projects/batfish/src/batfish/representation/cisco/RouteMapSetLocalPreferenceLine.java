package batfish.representation.cisco;

import batfish.representation.Configuration;
import batfish.representation.PolicyMapSetLine;
import batfish.representation.PolicyMapSetLocalPreferenceLine;

public class RouteMapSetLocalPreferenceLine extends RouteMapSetLine {

   private int _localPreference;

   public RouteMapSetLocalPreferenceLine(int localPreference) {
      _localPreference = localPreference;
   }

   public int getLocalPreference() {
      return _localPreference;
   }

   @Override
   public PolicyMapSetLine toPolicyMapSetLine(Configuration c) {
      return new PolicyMapSetLocalPreferenceLine(_localPreference);
   }
   
}
