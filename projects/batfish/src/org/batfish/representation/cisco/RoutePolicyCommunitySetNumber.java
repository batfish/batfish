package org.batfish.representation.cisco;

import java.util.Set;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.expr.ExplicitCommunitySet;
import org.batfish.main.Warnings;

public class RoutePolicyCommunitySetNumber extends RoutePolicyCommunitySet {

   private static final long serialVersionUID = 1L;

   private Set<Long> _numbers;

   public RoutePolicyCommunitySetNumber(Set<Long> numbers) {
      _numbers = numbers;
   }

   public Set<Long> getNumbers() {
      return _numbers;
   }

   @Override
   public CommunitySetExpr toCommunitySetExpr(CiscoConfiguration cc,
         Configuration c, Warnings w) {
      return new ExplicitCommunitySet(_numbers);
   }

}
