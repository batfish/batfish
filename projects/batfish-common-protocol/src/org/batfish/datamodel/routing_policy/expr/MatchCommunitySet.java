package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;

public class MatchCommunitySet extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private CommunitySetExpr _expr;

   @JsonCreator
   public MatchCommunitySet() {
   }

   public MatchCommunitySet(CommunitySetExpr expr) {
      _expr = expr;
   }

   public CommunitySetExpr getExpr() {
      return _expr;
   }

   public void setExpr(CommunitySetExpr expr) {
      _expr = expr;
   }

}
