package org.batfish.representation.cisco;

public class RoutePolicySetLocalPref extends RoutePolicySetStatement {

   private static final long serialVersionUID = 1L;

   private int _pref;

   public RoutePolicySetLocalPref(int pref) {
      _pref = pref;
   }

   public int getLocalPref() {
      return _pref;
   }

   @Override
   public RoutePolicySetType getSetType() {
      return RoutePolicySetType.LOCAL_PREFERENCE;
   }

}
