package org.batfish.representation.cisco;

import java.io.Serializable;

public class RoutePolicySetLocalPref extends RoutePolicySetStatement {

   private static final long serialVersionUID = 1L;

   private int _pref;

   public RoutePolicySetLocalPref(int pref) {
   	_pref = pref;
   }


   public RoutePolicySetType getSetType() { return RoutePolicySetType.LOCAL_PREFERENCE; }

   public int getLocalPref() { return _pref; }

}
