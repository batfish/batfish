package org.batfish.representation;

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

}
