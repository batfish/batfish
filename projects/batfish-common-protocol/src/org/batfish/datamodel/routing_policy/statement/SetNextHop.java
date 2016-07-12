package org.batfish.datamodel.routing_policy.statement;

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
