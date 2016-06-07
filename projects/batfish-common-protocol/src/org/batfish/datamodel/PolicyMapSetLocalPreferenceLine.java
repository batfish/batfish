package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PolicyMapSetLocalPreferenceLine extends PolicyMapSetLine {

   private static final String LOCAL_PREFERENCE_VAR = "localPreference";

   private static final long serialVersionUID = 1L;

   private final int _localPreference;

   @JsonCreator
   public PolicyMapSetLocalPreferenceLine(
         @JsonProperty(LOCAL_PREFERENCE_VAR) int localPreference) {
      _localPreference = localPreference;
   }

   @JsonProperty(LOCAL_PREFERENCE_VAR)
   public int getLocalPreference() {
      return _localPreference;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.LOCAL_PREFERENCE;
   }

}
