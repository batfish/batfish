package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

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

   @Override
   public Result evaluate(Environment environment) {
      Result result = new Result();
      boolean match = false;
      if (environment.getOriginalRoute() instanceof BgpRoute) {
         BgpRoute bgpRoute = (BgpRoute) environment.getOriginalRoute();
         match = _expr.matchSingleCommunity(environment,
               bgpRoute.getCommunities());
      }
      result.setBooleanValue(match);
      return result;
   }

   public CommunitySetExpr getExpr() {
      return _expr;
   }

   public void setExpr(CommunitySetExpr expr) {
      _expr = expr;
   }

}
