package org.batfish.representation.cisco;

public class RoutePolicySetCommunity extends RoutePolicySetStatement {

   private static final long serialVersionUID = 1L;

   private boolean _additive;
   private RoutePolicyCommunitySet _commSet;

   public RoutePolicySetCommunity(RoutePolicyCommunitySet commSet,
         boolean additive) {
      _commSet = commSet;
      _additive = additive;
   }

   public boolean getAdditive() {
      return _additive;
   }

   public RoutePolicyCommunitySet getCommunitySet() {
      return _commSet;
   }

   @Override
   public RoutePolicySetType getSetType() {
      return RoutePolicySetType.COMMUNITY;
   }

}
