package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.collections.CommunitySet;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
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

   @Override
   public Result execute(Environment environment) {
      BgpRoute.Builder outputRouteBuilder = (BgpRoute.Builder) environment
            .getOutputRoute();
      CommunitySet currentCommunities = outputRouteBuilder.getCommunities();
      CommunitySet matchingCommunities = _expr.communities(environment,
            currentCommunities);
      outputRouteBuilder.getCommunities().removeAll(matchingCommunities);
      Result result = new Result();
      return result;
   }

   public CommunitySetExpr getExpr() {
      return _expr;
   }

   public void setExpr(CommunitySetExpr expr) {
      _expr = expr;
   }

}
