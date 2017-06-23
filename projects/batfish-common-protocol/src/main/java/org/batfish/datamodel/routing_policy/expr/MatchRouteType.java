package org.batfish.datamodel.routing_policy.expr;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class MatchRouteType extends BooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private RouteTypeExpr _type;

   @JsonCreator
   private MatchRouteType() {
   }

   public MatchRouteType(RouteTypeExpr type) {
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
      MatchRouteType other = (MatchRouteType) obj;
      if (_type == null) {
         if (other._type != null) {
            return false;
         }
      }
      else if (!_type.equals(other._type)) {
         return false;
      }
      return true;
   }

   @Override
   public Result evaluate(Environment environment) {
      RouteType type = _type.evaluate(environment);
      throw new BatfishException(
            "unimplemented: match route type: " + type.routeTypeName());
   }

   public RouteTypeExpr getType() {
      return _type;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_type == null) ? 0 : _type.hashCode());
      return result;
   }

   public void setType(RouteTypeExpr type) {
      _type = type;
   }

}
