package org.batfish.representation.cisco;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.MatchRouteType;
import org.batfish.datamodel.routing_policy.expr.RouteTypeExpr;
import org.batfish.main.Warnings;

import com.fasterxml.jackson.annotation.JsonCreator;

public class RoutePolicyBooleanRouteTypeIs extends RoutePolicyBoolean {

   private static final long serialVersionUID = 1L;

   private RouteTypeExpr _type;

   @JsonCreator
   public RoutePolicyBooleanRouteTypeIs() {
   }

   public RoutePolicyBooleanRouteTypeIs(RouteTypeExpr type) {
      _type = type;
   }

   public RouteTypeExpr getType() {
      return _type;
   }

   public void setType(RouteTypeExpr type) {
      _type = type;
   }

   @Override
   public BooleanExpr toBooleanExpr(CiscoConfiguration cc, Configuration c,
         Warnings w) {
      return new MatchRouteType(_type);
   }
}
