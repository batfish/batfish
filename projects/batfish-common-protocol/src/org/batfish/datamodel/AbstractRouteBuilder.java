package org.batfish.datamodel;

public abstract class AbstractRouteBuilder<T extends AbstractRoute> {

   protected int _admin;

   protected int _metric;

   protected Prefix _network;

   protected Ip _nextHopIp;

   public abstract T build();

   public final Integer getAdmin() {
      return _admin;
   }

   public final Integer getMetric() {
      return _metric;
   }

   public final Prefix getNetwork() {
      return _network;
   }

   public final Ip getNextHopIp() {
      return _nextHopIp;
   }

   public final void setAdmin(int admin) {
      _admin = admin;
   }

   public final void setMetric(int metric) {
      _metric = metric;
   }

   public final void setNetwork(Prefix network) {
      _network = network;
   }

   public final void setNextHopIp(Ip nextHopIp) {
      _nextHopIp = nextHopIp;
   }

}
