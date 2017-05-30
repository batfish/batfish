package org.batfish.representation.cisco;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.expr.NamedCommunitySet;
import org.batfish.common.Warnings;

public class RoutePolicyCommunitySetName extends RoutePolicyCommunitySet {

   private static final long serialVersionUID = 1L;

   private String _name;

   public RoutePolicyCommunitySetName(String name) {
      this._name = name;
   }

   public String getName() {
      return this._name;
   }

   @Override
   public CommunitySetExpr toCommunitySetExpr(CiscoConfiguration cc,
         Configuration c, Warnings w) {
      return new NamedCommunitySet(_name);
   }

}
