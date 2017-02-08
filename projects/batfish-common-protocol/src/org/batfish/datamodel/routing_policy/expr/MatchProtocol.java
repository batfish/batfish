package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class MatchProtocol extends BooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private RoutingProtocol _protocol;

   @JsonCreator
   private MatchProtocol() {
   }

   public MatchProtocol(RoutingProtocol protocol) {
      _protocol = protocol;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      MatchProtocol other = (MatchProtocol) obj;
      if (_protocol != other._protocol) {
         return false;
      }
      return true;
   }

   @Override
   public Result evaluate(Environment environment) {
      Result result = new Result();
      boolean value = environment.getOriginalRoute().getProtocol()
            .equals(_protocol);
      result.setBooleanValue(value);
      return result;
   }

   public RoutingProtocol getProtocol() {
      return _protocol;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
            + ((_protocol == null) ? 0 : _protocol.hashCode());
      return result;
   }

   public void setProtocol(RoutingProtocol protocol) {
      _protocol = protocol;
   }

   @Override
   public String toString() {
      return getClass().getSimpleName() + "<" + _protocol.protocolName() + ">";
   }

}
