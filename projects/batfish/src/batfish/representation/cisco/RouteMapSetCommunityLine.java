package batfish.representation.cisco;

import java.util.List;

import batfish.representation.Configuration;
import batfish.representation.PolicyMapSetCommunityLine;
import batfish.representation.PolicyMapSetLine;

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
   public PolicyMapSetLine toPolicyMapSetLine(Configuration c) {
      return new PolicyMapSetCommunityLine(_communities);
   }

   @Override
   public RouteMapSetType getType(){
      return RouteMapSetType.COMMUNITY;
   }
   
}
