package org.batfish.representation;

public class StaticRoute extends Route {

   private static final long serialVersionUID = 1L;

   private int _administrativeCost;
   private String _nextHopInterface;
   private int _tag;

   public StaticRoute(Prefix prefix, Ip nextHopIp, String nextHopInterface,
         int administrativeCost, int tag) {
      super(prefix, nextHopIp);
      _nextHopInterface = nextHopInterface;
      _administrativeCost = administrativeCost;
      _tag = tag;
   }

   @Override
   public boolean equals(Object o) {
      StaticRoute rhs = (StaticRoute) o;
      boolean res = _prefix.equals(rhs._prefix);
      if (_nextHopIp != null) {
         res = res && _nextHopIp.equals(rhs._nextHopIp);
      }
      else {
         res = res && rhs._nextHopIp == null;
      }
      if (_nextHopInterface != null) {
         return res && _nextHopInterface.equals(rhs._nextHopInterface);
      }
      else {
         return res && rhs._nextHopInterface == null;
      }
   }

   @Override
   public int getAdministrativeCost() {
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

   @Override
   public int hashCode() {
      int code = _prefix.hashCode();
      if (_nextHopInterface != null) {
         code = code * 31 + _nextHopInterface.hashCode();
      }
      if (_nextHopIp != null) {
         code = code * 31 + _nextHopIp.hashCode();
      }
      return code;
   }

}
