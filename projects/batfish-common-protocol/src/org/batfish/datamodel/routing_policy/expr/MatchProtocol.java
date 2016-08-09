package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Route;
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
   public Result evaluate(Environment environment, Route route) {
      Result result = new Result();
      boolean value = route.getProtocol().equals(_protocol);
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
