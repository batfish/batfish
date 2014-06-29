package batfish.representation.cisco;

import java.util.Set;

public class RouteMapMatchAsPathAccessListLine extends RouteMapMatchLine {

   private Set<String> _listNames;

   public RouteMapMatchAsPathAccessListLine(Set<String> names) {
      _listNames = names;
   }

   public Set<String> getListNames() {
      return _listNames;
   }

   @Override
   public RouteMapMatchType getType() {
      return RouteMapMatchType.AS_PATH_ACCESS_LIST;
   }

}
