package batfish.representation;

public class StaticRoute extends Route {

   private static final long serialVersionUID = 1L;

   private int _administrativeCost;
   private String _nextHopInterface;
   private int _tag;

   public StaticRoute(Ip prefix, int prefixLength, Ip nextHopIp,
         String nextHopInterface, int administrativeCost, int tag) {
      super(prefix, prefixLength, nextHopIp);
      _nextHopInterface = nextHopInterface;
      _administrativeCost = administrativeCost;
      _tag = tag;
   }

   @Override
   public boolean equals(Object o) {
      StaticRoute rhs = (StaticRoute) o;
      boolean res = _prefix.equals(rhs._prefix)
            && _prefixLength == rhs._prefixLength;
      if (_nextHopIp != null) {
         return res && _nextHopIp.equals(rhs._nextHopIp);
      }
      else {
         return res && rhs._nextHopIp == null;
      }

   }

   @Override
   public int getAdministrativeCost() {
      return _administrativeCost;
   }

   public int getDistance() {
      return _administrativeCost;
   }

   public String getNextHopInterface() {
      return _nextHopInterface;
   }

   @Override
   public RouteType getRouteType() {
      return RouteType.STATIC;
   }

   public int getTag() {
      return _tag;
   }

   public boolean sameParseTree(StaticRoute route) {
      boolean res = equals(route)
            && (_administrativeCost == route._administrativeCost);
      if (_nextHopInterface != null) {
         return res && _nextHopInterface.equals(route._nextHopInterface);
      }
      else {
         return res && route._nextHopInterface == null;
      }

   }

}
