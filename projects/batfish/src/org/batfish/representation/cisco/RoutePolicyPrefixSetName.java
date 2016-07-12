package org.batfish.representation.cisco;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.expr.PrefixSetExpr;
import org.batfish.main.Warnings;

public class RoutePolicyPrefixSetName extends RoutePolicyPrefixSet {

   private static final long serialVersionUID = 1L;

   private String _name;

   public RoutePolicyPrefixSetName(String name) {
      _name = name;
   }

   public String getName() {
      return _name;
   }

   @Override
   public PrefixSetExpr toPrefixSetExpr(CiscoConfiguration cc, Configuration c,
         Warnings w) {
      return new NamedPrefixSet(_name);
   }

}
