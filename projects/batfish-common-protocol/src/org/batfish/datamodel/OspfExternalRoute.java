package org.batfish.datamodel;

public abstract class OspfExternalRoute extends OspfRoute {

   public static class Builder extends AbstractRouteBuilder<OspfExternalRoute> {

      private String _advertiser;

      private Integer _costToAdvertiser;

      private OspfMetricType _ospfMetricType;

      @Override
      public OspfExternalRoute build() {
         RoutingProtocol protocol = _ospfMetricType.toRoutingProtocol();
         OspfExternalRoute route;
         if (protocol == RoutingProtocol.OSPF_E1) {
            route = new OspfExternalType1Route(_network, _nextHopIp, _admin,
                  _metric, _advertiser);
         }
         else {
            route = new OspfExternalType2Route(_network, _nextHopIp, _admin,
                  _metric, _costToAdvertiser, _advertiser);
         }
         return route;
      }

      public String getAdvertiser() {
         return _advertiser;
      }

      public Integer getCostToAdvertiser() {
         return _costToAdvertiser;
      }

      public OspfMetricType getOspfMetricType() {
         return _ospfMetricType;
      }

      public void setAdvertiser(String advertiser) {
         _advertiser = advertiser;
      }

      public void setCostToAdvertiser(int costToAdvertiser) {
         _costToAdvertiser = costToAdvertiser;
      }

      public void setOspfMetricType(OspfMetricType ospfMetricType) {
         _ospfMetricType = ospfMetricType;
      }

   }

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final String _advertiser;

   private final OspfMetricType _ospfMetricType;

   public OspfExternalRoute(Prefix prefix, Ip nextHopIp, int admin, int metric,
         OspfMetricType ospfMetricType, String advertiser) {
      super(prefix, nextHopIp, admin, metric);
      _advertiser = advertiser;
      _ospfMetricType = ospfMetricType;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      OspfExternalRoute other = (OspfExternalRoute) obj;
      if (!_network.equals(other._network)) {
         return false;
      }
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
      if (_metric != other._metric) {
         return false;
      }
      if (_ospfMetricType != other._ospfMetricType) {
         return false;
      }
      return true;
   }

   public String getAdvertiser() {
      return _advertiser;
   }

   @Override
   public String getNextHopInterface() {
      return null;
   }

   public OspfMetricType getOspfMetricType() {
      return _ospfMetricType;
   }

   @Override
   public RoutingProtocol getProtocol() {
      return _ospfMetricType.toRoutingProtocol();
   }

   @Override
   public int getTag() {
      return -1;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _network.hashCode();
      result = prime * result
            + ((_nextHopIp == null) ? 0 : _nextHopIp.hashCode());
      result = prime * result + _admin;
      result = prime * result + _metric;
      result = prime * result
            + ((_ospfMetricType == null) ? 0 : _ospfMetricType.hashCode());
      return result;
   }

}
