package org.batfish.representation.cisco;

import java.util.List;

import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapSetAddCommunityLine;
import org.batfish.representation.PolicyMapSetLine;

public class RouteMapSetAdditiveCommunityLine extends RouteMapSetLine {

   private static final long serialVersionUID = 1L;

   private List<Long> _communities;

   public RouteMapSetAdditiveCommunityLine(List<Long> communities) {
      _communities = communities;
   }

   public List<Long> getCommunities() {
      return _communities;
   }

   @Override
   public RouteMapSetType getType() {
      return RouteMapSetType.ADDITIVE_COMMUNITY;
   }

   @Override
   public PolicyMapSetLine toPolicyMapSetLine(Configuration c) {
      return new PolicyMapSetAddCommunityLine(_communities);
   }

}
