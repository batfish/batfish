package batfish.representation.juniper;

import batfish.representation.PolicyMapSetLine;

public final class PsThenLocalPreference extends PsThen {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _localPreference;

   public PsThenLocalPreference(int localPreference) {
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
