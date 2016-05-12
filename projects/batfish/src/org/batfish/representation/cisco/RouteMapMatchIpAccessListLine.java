package org.batfish.representation.cisco;

import java.util.Set;

public class RouteMapMatchIpAccessListLine extends RouteMapMatchLine {

   private static final long serialVersionUID = 1L;

   private Set<String> _listNames;

   private boolean _routing;

   public RouteMapMatchIpAccessListLine(Set<String> names) {
      _listNames = names;
   }

   public Set<String> getListNames() {
      return _listNames;
   }

   public boolean getRouting() {
      return _routing;
   }

   @Override
   public RouteMapMatchType getType() {
      return RouteMapMatchType.IP_ACCESS_LIST;
   }

   public void setRouting(boolean routing) {
      _routing = routing;
   }

}
