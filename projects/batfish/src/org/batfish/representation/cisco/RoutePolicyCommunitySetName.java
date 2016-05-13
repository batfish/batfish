package org.batfish.representation.cisco;

public class RoutePolicyCommunitySetName extends RoutePolicyCommunitySet {

   private static final long serialVersionUID = 1L;

   private String _name;

   public RoutePolicyCommunitySetName(String name) {
      this._name = name;
   }

   @Override
   public RoutePolicyCommunityType getCommunityType() {
      return RoutePolicyCommunityType.NAME;
   }

   public String getName() {
      return this._name;
   }

}
