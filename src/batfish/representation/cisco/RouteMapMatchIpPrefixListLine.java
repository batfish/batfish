package batfish.representation.cisco;

import java.util.List;

public class RouteMapMatchIpPrefixListLine extends RouteMapMatchLine {

   private List<String> _listNames;

   public RouteMapMatchIpPrefixListLine(List<String> listNames) {
      _listNames = listNames;
   }

   @Override
   public RouteMapMatchType getType() {
      return RouteMapMatchType.IP_PREFIX_LIST;
   }

   public List<String> getListNames() {
      return _listNames;
   }
   
}
