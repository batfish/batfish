package batfish.representation;

import batfish.util.Util;

public class StaticRoute extends Route {

   private static final long serialVersionUID = 1L;

   private int _administrativeCost;
   private String _nextHopInterface;

   public StaticRoute(Ip prefix, int prefixLength, Ip nextHopIp,
         String nextHopInterface, int administrativeCost) {
      super(prefix, prefixLength, nextHopIp);
      _nextHopInterface = nextHopInterface;
      _administrativeCost = administrativeCost;
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

   @Override
   public String getIFString(int indentLevel) {
      return Util.getIndentString(indentLevel)
            + "StaticRoute "
            + String.format("%s AdminCost %s NextHopIface %s",
                  getRouteString(), _administrativeCost, _nextHopInterface);
   }

   public String getNextHopInterface() {
      return _nextHopInterface;
   }

   @Override
   public RouteType getRouteType() {
      return RouteType.STATIC;
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
