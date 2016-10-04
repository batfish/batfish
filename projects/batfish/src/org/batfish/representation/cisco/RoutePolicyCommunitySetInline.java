package org.batfish.representation.cisco;

import java.util.Set;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.CommunitySetElem;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.expr.InlineCommunitySet;
import org.batfish.main.Warnings;

public class RoutePolicyCommunitySetInline extends RoutePolicyCommunitySet {

   private static final long serialVersionUID = 1L;

   private Set<CommunitySetElem> _elems;

   public RoutePolicyCommunitySetInline(Set<CommunitySetElem> elems) {
      _elems = elems;
   }

   public Set<CommunitySetElem> getNumbers() {
      return _elems;
   }

   @Override
   public CommunitySetExpr toCommunitySetExpr(CiscoConfiguration cc,
         Configuration c, Warnings w) {
      return new InlineCommunitySet(_elems);
   }

}
