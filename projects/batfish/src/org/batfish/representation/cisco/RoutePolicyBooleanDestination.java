package org.batfish.representation.cisco;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.main.Warnings;

public class RoutePolicyBooleanDestination extends RoutePolicyBoolean {

   private static final long serialVersionUID = 1L;

   private RoutePolicyPrefixSet _prefixSet;

   public RoutePolicyBooleanDestination(RoutePolicyPrefixSet prefixSet) {
      _prefixSet = prefixSet;
   }

   public RoutePolicyPrefixSet getPrefixSet() {
      return _prefixSet;
   }

   @Override
   public RoutePolicyBooleanType getType() {
      return RoutePolicyBooleanType.DESTINATION;
   }

   @Override
   public BooleanExpr toBooleanExpr(CiscoConfiguration cc, Configuration c,
         Warnings w) {
      return new MatchPrefixSet(_prefixSet.toPrefixSetExpr(cc, c, w));
   }

}
