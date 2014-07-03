package batfish.representation.cisco;

import java.util.Set;

public class RouteMapMatchCommunityListLine extends RouteMapMatchLine {

   private Set<String> _listNames;

   public RouteMapMatchCommunityListLine(Set<String> names) {
      _listNames = names;
   }

   public Set<String> getListNames() {
      return _listNames;
   }

   @Override
   public RouteMapMatchType getType() {
      return RouteMapMatchType.COMMUNITY_LIST;
   }

}
