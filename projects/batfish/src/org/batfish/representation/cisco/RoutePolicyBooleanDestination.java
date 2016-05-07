package org.batfish.representation.cisco;

import java.io.Serializable;

public class RoutePolicyBooleanDestination extends RoutePolicyBoolean {

   private static final long serialVersionUID = 1L;
   
   private RoutePolicyPrefixSet _prefixSet;

   public RoutePolicyBooleanDestination(RoutePolicyPrefixSet prefixSet) {
   		_prefixSet = prefixSet;
   }

   public RoutePolicyBooleanType getType() { 
   	return RoutePolicyBooleanType.DESTINATION; }

   public RoutePolicyPrefixSet getPrefixSet() { return _prefixSet; }

}
