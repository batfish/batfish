package org.batfish.datamodel;

public class ConnectedRoute extends AbstractRoute
      implements Comparable<ConnectedRoute> {

   private static final long serialVersionUID = 1L;

   private final String _nextHopInterface;

   public ConnectedRoute(Prefix prefix, String nextHopInterface) {
      super(prefix, Route.UNSET_ROUTE_NEXT_HOP_IP);
      _nextHopInterface = nextHopInterface;
   }

   @Override
   public int compareTo(ConnectedRoute rhs) {
      int ret;
      ret = _network.compareTo(rhs._network);
      if (ret != 0) {
         return ret;
      }
      return _nextHopInterface.compareTo(rhs._nextHopInterface);
   }

   @Override
   public boolean equals(Object o) {
      ConnectedRoute rhs = (ConnectedRoute) o;
      boolean res = _network.equals(rhs._network);
      return res && _nextHopInterface.equals(rhs._nextHopInterface);
   }

   @Override
   public int getAdministrativeCost() {
      return 0;
   }

   @Override
   public Integer getMetric() {
      return 0;
   }

   @Override
   public String getNextHopInterface() {
      return _nextHopInterface;
   }

   @Override
   public RoutingProtocol getProtocol() {
      return RoutingProtocol.CONNECTED;
   }

   @Override
   public int getTag() {
      return NO_TAG;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _network.hashCode();
      result = prime * result + _nextHopInterface.hashCode();
      return result;
   }

   @Override
   protected String protocolRouteString() {
      return "";
   }

}
