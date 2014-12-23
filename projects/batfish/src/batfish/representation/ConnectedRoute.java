package batfish.representation;

public class ConnectedRoute extends Route {

   private static final long serialVersionUID = 1L;

   public ConnectedRoute(Ip prefix, int prefixLength, Ip nextHopIp) {
      super(prefix, prefixLength, nextHopIp);
   }

   @Override
   public boolean equals(Object o) {
      ConnectedRoute rhs = (ConnectedRoute) o;
      return _prefix.equals(rhs._prefix) && _prefixLength == rhs._prefixLength
            && _nextHopIp.equals(rhs._nextHopIp);
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
