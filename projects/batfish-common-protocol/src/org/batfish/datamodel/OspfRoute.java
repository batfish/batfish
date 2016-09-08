package org.batfish.datamodel;

public abstract class OspfRoute extends AbstractRoute {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   protected final int _admin;

   protected final int _metric;

   public OspfRoute(Prefix prefix, Ip nextHopIp, int admin, int metric) {
      super(prefix, nextHopIp);
      _admin = admin;
      _metric = metric;
   }

   @Override
   public final int getAdministrativeCost() {
      return _admin;
   }

   @Override
   public final Integer getMetric() {
      return _metric;
   }

}
