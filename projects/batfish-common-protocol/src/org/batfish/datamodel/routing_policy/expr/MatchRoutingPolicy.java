package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;

public class MatchRoutingPolicy extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private String _routingPolicy;

   @JsonCreator
   public MatchRoutingPolicy() {
   }

   public MatchRoutingPolicy(String routingPolicy) {
      _routingPolicy = routingPolicy;
   }

   public String getList() {
      return _routingPolicy;
   }

   public void setList(String routingPolicy) {
      _routingPolicy = routingPolicy;
   }

}
