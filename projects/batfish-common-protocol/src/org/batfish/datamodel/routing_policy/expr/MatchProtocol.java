package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.RoutingProtocol;

import com.fasterxml.jackson.annotation.JsonCreator;

public class MatchProtocol extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private RoutingProtocol _protocol;

   @JsonCreator
   public MatchProtocol() {
   }

   public MatchProtocol(RoutingProtocol protocol) {
      _protocol = protocol;
   }

   public RoutingProtocol getProtocol() {
      return _protocol;
   }

   public void setProtocol(RoutingProtocol protocol) {
      _protocol = protocol;
   }

}
