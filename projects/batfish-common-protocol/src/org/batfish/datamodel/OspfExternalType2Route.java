package org.batfish.datamodel;

public class OspfExternalType2Route extends OspfExternalRoute {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _costToAdvertiser;

   public OspfExternalType2Route(Prefix prefix, Ip nextHopIp, int admin,
         int metric, int costToAdvertiser, String advertiser) {
      super(prefix, nextHopIp, admin, metric, OspfMetricType.E2, advertiser);
      _costToAdvertiser = costToAdvertiser;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (!super.equals(obj)) {
         return false;
      }
      OspfExternalType2Route other = (OspfExternalType2Route) obj;
      if (_costToAdvertiser != other._costToAdvertiser) {
         return false;
      }
      return true;
   }

   public int getCostToAdvertiser() {
      return _costToAdvertiser;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + _costToAdvertiser;
      return result;
   }

   @Override
   protected final String ospfExternalRouteString() {
      return " costToAdvertiser:" + _costToAdvertiser;
   }

}
