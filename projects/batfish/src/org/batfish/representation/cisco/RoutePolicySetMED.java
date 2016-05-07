package org.batfish.representation.cisco;

import java.io.Serializable;

public class RoutePolicySetMED extends RoutePolicySetStatement {

   private static final long serialVersionUID = 1L;

   private int _pref;

   public RoutePolicySetMED(int pref) {
   	_pref = pref;
   }


   public RoutePolicySetType getSetType() { return RoutePolicySetType.MED; }

   public int getLocalPref() { return _pref; }

}
