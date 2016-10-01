package org.batfish.datamodel.routing_policy.statement;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.NextHopExpr;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SetNextHop extends AbstractStatement {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private boolean _destinationVrf;

   private NextHopExpr _expr;

   @JsonCreator
   public SetNextHop() {
   }

   public SetNextHop(NextHopExpr expr, boolean destinationVrf) {
      _expr = expr;
      _destinationVrf = destinationVrf;
   }

   @Override
   public Result execute(Environment environment) {
      Result result = new Result();
      environment.getOutputRoute()
            .setNextHopIp(_expr.getNextHopIp(environment));
      return result;
   }

   public boolean getDestinationVrf() {
      return _destinationVrf;
   }

   public NextHopExpr getExpr() {
      return _expr;
   }

   public void setDestinationVrf(boolean destinationVrf) {
      _destinationVrf = destinationVrf;
   }

   public void setExpr(NextHopExpr expr) {
      _expr = expr;
   }

}
