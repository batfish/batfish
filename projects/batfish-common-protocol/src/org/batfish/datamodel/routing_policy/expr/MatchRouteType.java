package org.batfish.datamodel.routing_policy.expr;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class MatchRouteType extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private RouteTypeExpr _type;

   @JsonCreator
   public MatchRouteType() {
   }

   public MatchRouteType(RouteTypeExpr type) {
      _type = type;
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

   public void setType(RouteTypeExpr type) {
      _type = type;
   }

}
