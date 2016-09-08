package org.batfish.representation.cisco;

import java.util.List;
import java.util.TreeSet;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PolicyMapSetAddCommunityLine;
import org.batfish.datamodel.PolicyMapSetLine;
import org.batfish.datamodel.routing_policy.expr.ExplicitCommunitySet;
import org.batfish.datamodel.routing_policy.statement.AddCommunity;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.main.Warnings;

public class RouteMapSetAdditiveCommunityLine extends RouteMapSetLine {

   private static final long serialVersionUID = 1L;

   private List<Long> _communities;

   public RouteMapSetAdditiveCommunityLine(List<Long> communities) {
      _communities = communities;
   }

   @Override
   public void applyTo(List<Statement> statements, CiscoConfiguration cc,
         Configuration c, Warnings w) {
      statements.add(new AddCommunity(
            new ExplicitCommunitySet(new TreeSet<>(_communities))));
   }

   public List<Long> getCommunities() {
      return _communities;
   }

   @Override
   public RouteMapSetType getType() {
      return RouteMapSetType.ADDITIVE_COMMUNITY;
   }

   @Override
   public PolicyMapSetLine toPolicyMapSetLine(CiscoConfiguration v,
         Configuration c, Warnings w) {
      return new PolicyMapSetAddCommunityLine(_communities);
   }

}
