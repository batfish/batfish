package org.batfish.datamodel;

public class OspfExternalType1Route extends OspfExternalRoute {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public OspfExternalType1Route(Prefix prefix, Ip nextHopIp, int admin,
         int metric, String advertiser) {
      super(prefix, nextHopIp, admin, metric, OspfMetricType.E1, advertiser);
   }

   @Override
   protected final String ospfExternalRouteString() {
      return "";
   }

}
