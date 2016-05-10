package org.batfish.representation.cisco;

public class RoutePolicyPrefixSetName extends RoutePolicyPrefixSet {

   private static final long serialVersionUID = 1L;

   private String _name;

   public RoutePolicyPrefixSetName(String name) {
      _name = name;
   }

   public String getName() {
      return _name;
   }

   @Override
   public RoutePolicyPrefixType getPrefixType() {
      return RoutePolicyPrefixType.NAME;
   }

}
