package org.batfish.representation.cisco;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.HasRoute;
import org.batfish.main.Warnings;

public class RoutePolicyBooleanRIBHasRoute extends RoutePolicyBoolean {

   private static final long serialVersionUID = 1L;

   private RoutePolicyPrefixSet _prefixSet;

   public RoutePolicyBooleanRIBHasRoute(RoutePolicyPrefixSet prefixSet) {
      _prefixSet = prefixSet;
   }

   public RoutePolicyPrefixSet getPrefixSet() {
      return _prefixSet;
   }

   @Override
   public RoutePolicyBooleanType getType() {
      return RoutePolicyBooleanType.RIB_HAS_ROUTE;
   }

   @Override
   public BooleanExpr toBooleanExpr(CiscoConfiguration cc, Configuration c,
         Warnings w) {
      return new HasRoute(_prefixSet.toPrefixSetExpr(cc, c, w));
   }

}
