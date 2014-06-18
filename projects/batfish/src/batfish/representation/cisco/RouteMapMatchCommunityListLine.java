package batfish.representation.cisco;

import java.util.List;

public class RouteMapMatchCommunityListLine extends RouteMapMatchLine {

   private List<String> _listNames;

   public RouteMapMatchCommunityListLine(List<String> nameLists) {
      _listNames = nameLists;
   }

   @Override
   public RouteMapMatchType getType() {
      return RouteMapMatchType.COMMUNITY_LIST;
   }

   public List<String> getListNames() {
      return _listNames;
   }

}
