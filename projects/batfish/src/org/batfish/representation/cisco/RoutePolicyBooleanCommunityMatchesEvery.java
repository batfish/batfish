package org.batfish.representation.cisco;

import java.io.Serializable;

public class RoutePolicyBooleanCommunityMatchesEvery extends RoutePolicyBoolean {

   private static final long serialVersionUID = 1L;
   
   private RoutePolicyCommunitySet _commSet;

   public RoutePolicyBooleanCommunityMatchesEvery(RoutePolicyCommunitySet commSet) {
   		_commSet = commSet;
   }

   public RoutePolicyBooleanType getType() { 
   	return RoutePolicyBooleanType.COMMUNITY_MATCHES_EVERY; }

   public RoutePolicyCommunitySet getCommSet() { return _commSet; }

}
