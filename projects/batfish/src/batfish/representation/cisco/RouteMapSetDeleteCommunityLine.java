package batfish.representation.cisco;

import batfish.representation.CommunityList;
import batfish.representation.Configuration;
import batfish.representation.PolicyMapSetDeleteCommunityLine;
import batfish.representation.PolicyMapSetLine;

public class RouteMapSetDeleteCommunityLine extends RouteMapSetLine {

   private static final long serialVersionUID = 1L;

   private String _listName;

   public RouteMapSetDeleteCommunityLine(String listName) {
      _listName = listName;
   }

   public String getListName() {
      return _listName;
   }

   @Override
   public RouteMapSetType getType() {
      return RouteMapSetType.DELETE_COMMUNITY;
   }

   @Override
   public PolicyMapSetLine toPolicyMapSetLine(Configuration c) {
      CommunityList dcList = c.getCommunityLists().get(_listName);
      return new PolicyMapSetDeleteCommunityLine(dcList);
   }

}
