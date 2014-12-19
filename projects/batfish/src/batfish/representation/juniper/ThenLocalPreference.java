package batfish.representation.juniper;

import batfish.representation.PolicyMapSetLine;

public final class ThenLocalPreference extends Then {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _localPreference;

   public ThenLocalPreference(int localPreference) {
      _localPreference = localPreference;
   }

   public int getLocalPreference() {
      return _localPreference;
   }

   @Override
   public PolicyMapSetLine toPolicyStatmentSetLine() {
      // TODO Auto-generated method stub
      return null;
   }

}
