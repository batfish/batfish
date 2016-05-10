package org.batfish.representation.cisco;

public class RoutePolicyBooleanCommunityMatchesEvery extends RoutePolicyBoolean {

   private static final long serialVersionUID = 1L;

   private RoutePolicyCommunitySet _commSet;

   public RoutePolicyBooleanCommunityMatchesEvery(
         RoutePolicyCommunitySet commSet) {
      _commSet = commSet;
   }

   public RoutePolicyCommunitySet getCommSet() {
      return _commSet;
   }

   @Override
   public RoutePolicyBooleanType getType() {
      return RoutePolicyBooleanType.COMMUNITY_MATCHES_EVERY;
   }

}
