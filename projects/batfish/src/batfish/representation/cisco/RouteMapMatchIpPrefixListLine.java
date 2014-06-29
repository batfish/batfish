package batfish.representation.cisco;

import java.util.Set;

public class RouteMapMatchIpPrefixListLine extends RouteMapMatchLine {

   private Set<String> _listNames;

   public RouteMapMatchIpPrefixListLine(Set<String> names) {
      _listNames = names;
   }

   public Set<String> getListNames() {
      return _listNames;
   }

   @Override
   public RouteMapMatchType getType() {
      return RouteMapMatchType.IP_PREFIX_LIST;
   }

}
