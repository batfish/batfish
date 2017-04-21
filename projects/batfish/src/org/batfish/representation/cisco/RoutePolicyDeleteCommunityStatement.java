package org.batfish.representation.cisco;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.DeleteCommunity;
import org.batfish.datamodel.routing_policy.statement.RetainCommunity;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.common.Warnings;

public class RoutePolicyDeleteCommunityStatement
      extends RoutePolicyDeleteStatement {

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

   @Override
   public Statement toSetStatement(CiscoConfiguration cc, Configuration c,
         Warnings w) {
      if (negated) {
         return new RetainCommunity(commset.toCommunitySetExpr(cc, c, w));
      }
      else {
         return new DeleteCommunity(commset.toCommunitySetExpr(cc, c, w));
      }
   }

}
