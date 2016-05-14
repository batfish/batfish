package org.batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.PolicyMapSetCommunityLine;
import org.batfish.datamodel.PolicyMapSetLine;
import org.batfish.main.Warnings;

public final class RouteMapSetCommunityListLine extends RouteMapSetLine {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Set<String> _communityLists;

   public RouteMapSetCommunityListLine(Set<String> communityLists) {
      _communityLists = communityLists;
   }

   @Override
   public RouteMapSetType getType() {
      return RouteMapSetType.COMMUNITY_LIST;
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
      PolicyMapSetLine policyMapSetLine = new PolicyMapSetCommunityLine(
            communities);
      return policyMapSetLine;
   }

}
