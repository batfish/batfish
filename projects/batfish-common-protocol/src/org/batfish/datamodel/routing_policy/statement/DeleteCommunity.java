package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;

import com.fasterxml.jackson.annotation.JsonCreator;

public class DeleteCommunity extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private CommunitySetExpr _expr;

   @JsonCreator
   public DeleteCommunity() {
   }

   public DeleteCommunity(CommunitySetExpr expr) {
      _expr = expr;
   }

   public CommunitySetExpr getExpr() {
      return _expr;
   }

   public void setExpr(CommunitySetExpr expr) {
      _expr = expr;
   }

}
