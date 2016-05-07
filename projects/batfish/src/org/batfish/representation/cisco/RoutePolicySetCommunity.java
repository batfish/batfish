package org.batfish.representation.cisco;

import java.io.Serializable;

public class RoutePolicySetCommunity extends RoutePolicySetStatement {

   private static final long serialVersionUID = 1L;

   private RoutePolicyCommunitySet _commSet; 
   private boolean _additive;

   public RoutePolicySetCommunity(RoutePolicyCommunitySet commSet, boolean additive) {
   	_commSet = commSet;
   	_additive = additive;
   }


   public RoutePolicySetType getSetType() { return RoutePolicySetType.COMMUNITY; }

   public RoutePolicyCommunitySet getCommunitySet() { return _commSet; }

   public boolean getAdditive() { return _additive; }

}
