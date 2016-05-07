package org.batfish.representation.cisco;

import java.io.Serializable;

public class RoutePolicyBooleanCommunityMatchesAny extends RoutePolicyBoolean {

   private static final long serialVersionUID = 1L;
   
   private RoutePolicyCommunitySet _commSet;

   public RoutePolicyBooleanCommunityMatchesAny(RoutePolicyCommunitySet commSet) {
   		_commSet = commSet;
   }

   public RoutePolicyBooleanType getType() { 
   	return RoutePolicyBooleanType.COMMUNITY_MATCHES_ANY; }

   public RoutePolicyCommunitySet getCommSet() { return _commSet; }

}
