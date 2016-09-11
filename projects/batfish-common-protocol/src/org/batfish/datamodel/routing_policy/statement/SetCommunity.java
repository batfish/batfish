package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SetCommunity extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private CommunitySetExpr _expr;

   @JsonCreator
   public SetCommunity() {
   }

   public SetCommunity(CommunitySetExpr expr) {
      _expr = expr;
   }

   @Override
   public Result execute(Environment environment,
         AbstractRouteBuilder<?> route) {
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
