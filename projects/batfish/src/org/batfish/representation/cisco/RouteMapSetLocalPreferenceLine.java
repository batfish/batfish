package org.batfish.representation.cisco;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PolicyMapSetLine;
import org.batfish.datamodel.PolicyMapSetLocalPreferenceLine;
import org.batfish.main.Warnings;

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
   public PolicyMapSetLine toPolicyMapSetLine(CiscoConfiguration v,
         Configuration c, Warnings w) {
      return new PolicyMapSetLocalPreferenceLine(_localPreference);
   }

}
