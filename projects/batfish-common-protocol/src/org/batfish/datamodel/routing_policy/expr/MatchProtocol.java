package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

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

   @Override
   public Result evaluate(Environment environment,
         AbstractRouteBuilder<?> outputRoute) {
      Result result = new Result();
      boolean value = environment.getOriginalRoute().getProtocol()
            .equals(_protocol);
      result.setBooleanValue(value);
      return result;
   }

   public RoutingProtocol getProtocol() {
      return _protocol;
   }

   public void setProtocol(RoutingProtocol protocol) {
      _protocol = protocol;
   }

}
