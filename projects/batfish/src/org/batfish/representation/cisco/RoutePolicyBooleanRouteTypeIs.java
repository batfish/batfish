package org.batfish.representation.cisco;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.HasRoute;
import org.batfish.main.Warnings;

public class RoutePolicyBooleanRouteTypeIs extends RoutePolicyBoolean {

   private static final long serialVersionUID = 1L;

   //Ari: implement the class
   
   @Override
   public BooleanExpr toBooleanExpr(CiscoConfiguration cc, Configuration c,
         Warnings w) {
      //Ari: this should be something else
      return new HasRoute();
   }
}
