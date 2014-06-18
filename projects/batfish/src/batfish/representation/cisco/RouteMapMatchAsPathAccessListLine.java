package batfish.representation.cisco;

import java.util.List;

public class RouteMapMatchAsPathAccessListLine extends RouteMapMatchLine {

   private List<String> _listNames;

   public RouteMapMatchAsPathAccessListLine(List<String> listNames) {
      _listNames = listNames;
   }

   @Override
   public RouteMapMatchType getType() {
      return RouteMapMatchType.AS_PATH_ACCESS_LIST;
   }

   public List<String> getListNames() {
      return _listNames;
   }
   
}
