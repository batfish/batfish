package org.batfish.representation.cisco;

public class RoutePolicySetMED extends RoutePolicySetStatement {

   private static final long serialVersionUID = 1L;

   private int _pref;

   public RoutePolicySetMED(int pref) {
      _pref = pref;
   }

   public int getLocalPref() {
      return _pref;
   }

   @Override
   public RoutePolicySetType getSetType() {
      return RoutePolicySetType.MED;
   }

}
