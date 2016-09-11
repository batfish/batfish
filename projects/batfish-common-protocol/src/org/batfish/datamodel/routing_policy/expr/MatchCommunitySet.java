package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.AbstractRouteBuilder;
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
   public Result evaluate(Environment environment,
         AbstractRouteBuilder<?> outputRoute) {
      Result result = new Result();
      BgpRoute bgpRoute = (BgpRoute) environment.getOriginalRoute();
      boolean match = _expr.matchSingleCommunity(environment,
            bgpRoute.getCommunities());
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
