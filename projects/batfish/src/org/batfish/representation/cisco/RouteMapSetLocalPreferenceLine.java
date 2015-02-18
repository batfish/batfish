package org.batfish.representation.cisco;

import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapSetLine;
import org.batfish.representation.PolicyMapSetLocalPreferenceLine;

public class RouteMapSetLocalPreferenceLine extends RouteMapSetLine {

   private static final long serialVersionUID = 1L;

   private int _localPreference;

   public RouteMapSetLocalPreferenceLine(int localPreference) {
      _localPreference = localPreference;
   }

   public int getLocalPreference() {
      return _localPreference;
   }

   @Override
   public RouteMapSetType getType() {
      return RouteMapSetType.LOCAL_PREFERENCE;
   }

   @Override
   public PolicyMapSetLine toPolicyMapSetLine(Configuration c) {
      return new PolicyMapSetLocalPreferenceLine(_localPreference);
   }

}
