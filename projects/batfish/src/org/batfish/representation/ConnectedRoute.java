package org.batfish.representation;

import org.batfish.common.datamodel.Ip;
import org.batfish.common.datamodel.Prefix;

public class ConnectedRoute extends Route {

   private static final long serialVersionUID = 1L;

   public ConnectedRoute(Prefix prefix, Ip nextHopIp) {
      super(prefix, nextHopIp);
   }

   @Override
   public boolean equals(Object o) {
      ConnectedRoute rhs = (ConnectedRoute) o;
      return _prefix.equals(rhs._prefix) && _nextHopIp.equals(rhs._nextHopIp);
   }

   @Override
   public int getAdministrativeCost() {
      return 0;
   }

   @Override
   public RouteType getRouteType() {
      return RouteType.CONNECTED;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _prefix.hashCode();
      result = prime * result + _nextHopIp.hashCode();
      return result;
   }

}
