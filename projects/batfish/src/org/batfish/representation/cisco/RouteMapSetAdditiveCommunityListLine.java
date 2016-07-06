package org.batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.PolicyMapSetAddCommunityLine;
import org.batfish.datamodel.PolicyMapSetLine;
import org.batfish.datamodel.routing_policy.statement.AddCommunity;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.main.Warnings;

public final class RouteMapSetAdditiveCommunityListLine extends RouteMapSetLine {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Set<String> _communityLists;

   public RouteMapSetAdditiveCommunityListLine(Set<String> communityLists) {
      _communityLists = communityLists;
   }

   @Override
   public void applyTo(List<Statement> statements, CiscoConfiguration cc,
         Configuration c, Warnings w) {
      SortedSet<Long> communities = new TreeSet<Long>();
      for (String communityListName : _communityLists) {
         CommunityList communityList = c.getCommunityLists().get(
               communityListName);
         if (communityList != null) {
            StandardCommunityList scl = cc.getStandardCommunityLists().get(
                  communityListName);
            if (scl != null) {
               for (StandardCommunityListLine line : scl.getLines()) {
                  if (line.getAction() == LineAction.ACCEPT) {
                     communities.addAll(line.getCommunities());
                  }
                  else {
                     w.redFlag("Expected only permit lines in standard community-list referred to by route-map set community community-list line: \""
                           + communityListName + "\"");
                  }
               }
            }
            else {
               w.redFlag("Expected standard community list in route-map set community community-list line but got expanded instead: \""
                     + communityListName + "\"");
            }
         }
         else {
            w.redFlag("Reference to undefined community list: \""
                  + communityListName + "\"");
         }
      }
      statements.add(new AddCommunity(communities));
   }

   @Override
   public RouteMapSetType getType() {
      return RouteMapSetType.ADDITIVE_COMMUNITY_LIST;
   }

   @Override
   public PolicyMapSetLine toPolicyMapSetLine(CiscoConfiguration v,
         Configuration c, Warnings w) {
      List<Long> communities = new ArrayList<Long>();
      for (String communityListName : _communityLists) {
         CommunityList communityList = c.getCommunityLists().get(
               communityListName);
         if (communityList != null) {
            StandardCommunityList scl = v.getStandardCommunityLists().get(
                  communityListName);
            if (scl != null) {
               for (StandardCommunityListLine line : scl.getLines()) {
                  if (line.getAction() == LineAction.ACCEPT) {
                     communities.addAll(line.getCommunities());
                  }
                  else {
                     w.redFlag("Expected only permit lines in standard community-list referred to by route-map set community community-list line: \""
                           + communityListName + "\"");
                  }
               }
            }
            else {
               w.redFlag("Expected standard community list in route-map set community community-list line but got expanded instead: \""
                     + communityListName + "\"");
            }
         }
         else {
            w.redFlag("Reference to undefined community list: \""
                  + communityListName + "\"");
         }
      }
      PolicyMapSetLine policyMapSetLine = new PolicyMapSetAddCommunityLine(
            communities);
      return policyMapSetLine;
   }

}
