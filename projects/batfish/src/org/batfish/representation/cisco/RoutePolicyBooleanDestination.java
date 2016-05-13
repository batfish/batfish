package org.batfish.representation.cisco;

public class RoutePolicyBooleanDestination extends RoutePolicyBoolean {

   private static final long serialVersionUID = 1L;

   private RoutePolicyPrefixSet _prefixSet;

   public RoutePolicyBooleanDestination(RoutePolicyPrefixSet prefixSet) {
      _prefixSet = prefixSet;
   }

   public RoutePolicyPrefixSet getPrefixSet() {
      return _prefixSet;
   }

   @Override
   public RoutePolicyBooleanType getType() {
      return RoutePolicyBooleanType.DESTINATION;
   }

}
