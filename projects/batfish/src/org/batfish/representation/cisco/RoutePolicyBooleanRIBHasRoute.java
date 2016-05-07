package org.batfish.representation.cisco;

import java.io.Serializable;

public class RoutePolicyBooleanRIBHasRoute extends RoutePolicyBoolean {

   private static final long serialVersionUID = 1L;
   
   private RoutePolicyPrefixSet _prefixSet;

   public RoutePolicyBooleanRIBHasRoute(RoutePolicyPrefixSet prefixSet) {
   		_prefixSet = prefixSet;
   }

   public RoutePolicyBooleanType getType() { 
   	return RoutePolicyBooleanType.RIB_HAS_ROUTE; }

   public RoutePolicyPrefixSet getPrefixSet() { return _prefixSet; }

}
