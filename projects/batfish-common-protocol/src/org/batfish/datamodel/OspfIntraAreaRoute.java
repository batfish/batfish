package org.batfish.datamodel;

public class OspfIntraAreaRoute extends OspfRoute {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final long _area;

   public OspfIntraAreaRoute(Prefix prefix, Ip nextHopIp, int admin, int metric,
         long area) {
      super(prefix, nextHopIp, admin, metric);
      _area = area;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      OspfIntraAreaRoute other = (OspfIntraAreaRoute) obj;
      if (_nextHopIp == null) {
         if (other._nextHopIp != null) {
            return false;
         }
      }
      else if (!_nextHopIp.equals(other._nextHopIp)) {
         return false;
      }
      if (_admin != other._admin) {
         return false;
      }
      if (_area != other._area) {
         return false;
      }
      if (_metric != other._metric) {
         return false;
      }
      return _network.equals(other._network);
   }

   public long getArea() {
      return _area;
   }

   @Override
   public String getNextHopInterface() {
      return null;
   }

   @Override
   public RoutingProtocol getProtocol() {
      return RoutingProtocol.OSPF;
   }

   @Override
   public int getTag() {
      return -1;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _admin;
      result = prime * result + (int) (_area ^ (_area >>> 32));
      result = prime * result + _metric;
      result = prime * result + _network.hashCode();
      result = prime * result
            + (_nextHopIp == null ? 0 : _nextHopIp.hashCode());
      return result;
   }

   @Override
   protected final String protocolRouteString() {
      return " area:" + _area;
   }

}
