package org.batfish.representation.cisco;

public class RoutePolicyCommunitySetNumber extends RoutePolicyCommunitySet {

   private static final long serialVersionUID = 1L;

   private String number;

   public RoutePolicyCommunitySetNumber(String number) {
      this.number = number;
   }

   @Override
   public RoutePolicyCommunityType getCommunityType() {
      return RoutePolicyCommunityType.NUMBER;
   }

   public String getNumber() {
      return this.number;
   }

}
