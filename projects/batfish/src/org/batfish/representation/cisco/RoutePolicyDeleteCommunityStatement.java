package org.batfish.representation.cisco;

public class RoutePolicyDeleteCommunityStatement extends
      RoutePolicyDeleteStatement {

   private static final long serialVersionUID = 1L;
   private RoutePolicyCommunitySet commset;

   private boolean negated;

   public RoutePolicyDeleteCommunityStatement(boolean negated,
         RoutePolicyCommunitySet commset) {
      this.negated = negated;
      this.commset = commset;
   }

   public RoutePolicyCommunitySet getCommSet() {
      return commset;
   }

   @Override
   public RoutePolicyDeleteType getDeleteType() {
      return RoutePolicyDeleteType.COMMUNITY;
   }

   public boolean getNegated() {
      return negated;
   }

}
