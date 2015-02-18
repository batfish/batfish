package org.batfish.representation;

public class PolicyMapSetLocalPreferenceLine extends PolicyMapSetLine {

   private static final long serialVersionUID = 1L;

   private int _localPreference;

   public PolicyMapSetLocalPreferenceLine(int localPreference) {
      _localPreference = localPreference;
   }

   public int getLocalPreference() {
      return _localPreference;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.LOCAL_PREFERENCE;
   }

}
