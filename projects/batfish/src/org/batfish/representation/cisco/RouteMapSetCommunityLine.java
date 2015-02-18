package org.batfish.representation.cisco;

import java.util.List;

import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapSetCommunityLine;
import org.batfish.representation.PolicyMapSetLine;

public class RouteMapSetCommunityLine extends RouteMapSetLine {

   private static final long serialVersionUID = 1L;

   private List<Long> _communities;

   public RouteMapSetCommunityLine(List<Long> communities) {
      _communities = communities;
   }

   public List<Long> getCommunities() {
      return _communities;
   }

   @Override
   public RouteMapSetType getType() {
      return RouteMapSetType.COMMUNITY;
   }

   @Override
   public PolicyMapSetLine toPolicyMapSetLine(Configuration c) {
      return new PolicyMapSetCommunityLine(_communities);
   }

}
