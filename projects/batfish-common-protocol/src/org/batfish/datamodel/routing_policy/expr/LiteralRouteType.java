package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class LiteralRouteType implements RouteTypeExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private RouteType _type;

   @JsonCreator
   public LiteralRouteType() {
   }

   public LiteralRouteType(RouteType type) {
      _type = type;
   }

   @Override
   public RouteType evaluate(Environment environment) {
      return _type;
   }

   public RouteType getType() {
      return _type;
   }

   public void setType(RouteType type) {
      _type = type;
   }

}
