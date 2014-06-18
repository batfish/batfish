package batfish.representation.cisco;

import java.util.List;

public class RouteMapMatchIpAccessListLine extends RouteMapMatchLine {

   private List<String> _listNames;

   public RouteMapMatchIpAccessListLine(List<String> listNames) {
      _listNames = listNames;
   }

   @Override
   public RouteMapMatchType getType() {
      return RouteMapMatchType.IP_ACCESS_LIST;
   }

   public List<String> getListNames() {
      return _listNames;
   }
   
}
