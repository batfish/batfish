package org.batfish.datamodel;

public abstract class AbstractRouteBuilder<T extends AbstractRoute> {

   protected Integer _admin;

   protected Integer _metric;

   protected Prefix _network;

   protected Ip _nextHopIp;

   public abstract T build();

   public Integer getAdmin() {
      return _admin;
   }

   public Integer getMetric() {
      return _metric;
   }

   public Prefix getNetwork() {
      return _network;
   }

   public Ip getNextHopIp() {
      return _nextHopIp;
   }

   public void setAdmin(Integer admin) {
      _admin = admin;
   }

   public void setMetric(Integer metric) {
      _metric = metric;
   }

   public void setNetwork(Prefix network) {
      _network = network;
   }

   public void setNextHopIp(Ip nextHopIp) {
      _nextHopIp = nextHopIp;
   }

}
