package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class LiteralRouteType extends RouteTypeExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private RouteType _type;

   @JsonCreator
   private LiteralRouteType() {
   }

   public LiteralRouteType(RouteType type) {
      _type = type;
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
      LiteralRouteType other = (LiteralRouteType) obj;
      if (_type != other._type) {
         return false;
      }
      return true;
   }

   @Override
   public RouteType evaluate(Environment environment) {
      return _type;
   }

   public RouteType getType() {
      return _type;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_type == null) ? 0 : _type.hashCode());
      return result;
   }

   public void setType(RouteType type) {
      _type = type;
   }

}
