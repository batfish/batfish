package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StaticRoute extends AbstractRoute
      implements Comparable<StaticRoute> {

   private static final String NEXT_HOP_INTERFACE_VAR = "nextHopInterface";

   private static final long serialVersionUID = 1L;

   private static final String TAG_VAR = "tag";

   private int _administrativeCost;

   private final String _nextHopInterface;

   private final int _tag;

   @JsonCreator
   public StaticRoute(@JsonProperty(NETWORK_VAR) Prefix network,
         @JsonProperty(NEXT_HOP_IP_VAR) Ip nextHopIp,
         @JsonProperty(NEXT_HOP_INTERFACE_VAR) String nextHopInterface,
         @JsonProperty(TAG_VAR) int tag) {
      super(network, nextHopIp);
      _nextHopInterface = nextHopInterface;
      _tag = tag;
   }

   public StaticRoute(Prefix network, Ip nextHopIp, String nextHopInterface,
         int administrativeCost, int tag) {
      super(network, nextHopIp);
      _nextHopInterface = nextHopInterface;
      _administrativeCost = administrativeCost;
      _tag = tag;
   }

   @Override
   public int compareTo(StaticRoute rhs) {
      int ret;
      ret = _network.compareTo(rhs._network);
      if (ret != 0) {
         return ret;
      }
      if (_nextHopInterface == null) {
         if (rhs._nextHopInterface != null) {
            return -1;
         }
      }
      else if (rhs._nextHopInterface == null) {
         return 1;
      }
      else {
         ret = _nextHopInterface.compareTo(rhs._nextHopInterface);
         if (ret != 0) {
            return ret;
         }
      }
      if (_nextHopIp == null) {
         if (rhs._nextHopIp != null) {
            return -1;
         }
      }
      else if (rhs._nextHopIp == null) {
         return 1;
      }
      else {
         ret = _nextHopIp.compareTo(rhs._nextHopIp);
         if (ret != 0) {
            return ret;
         }
      }
      return Integer.compare(_tag, rhs._tag);
   }

   @Override
   public boolean equals(Object o) {
      StaticRoute rhs = (StaticRoute) o;
      boolean res = _network.equals(rhs._network);
      res = res && _administrativeCost == rhs._administrativeCost;
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
         res = res && rhs._nextHopInterface == null;
      }
      return res && _tag == rhs._tag;
   }

   @Override
   @JsonProperty(ADMINISTRATIVE_COST_VAR)
   public int getAdministrativeCost() {
      return _administrativeCost;
   }

   @Override
   @JsonIgnore
   public Integer getMetric() {
      return 0;
   }

   @Override
   @JsonProperty(NEXT_HOP_INTERFACE_VAR)
   public String getNextHopInterface() {
      return _nextHopInterface;
   }

   @Override
   public RoutingProtocol getProtocol() {
      return RoutingProtocol.STATIC;
   }

   @Override
   @JsonProperty(TAG_VAR)
   public int getTag() {
      return _tag;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _network.hashCode();
      result = prime * result + _administrativeCost;
      result = prime * result
            + ((_nextHopInterface == null) ? 0 : _nextHopInterface.hashCode());
      result = prime * result
            + ((_nextHopIp == null) ? 0 : _nextHopIp.hashCode());
      result = prime * result + _tag;
      return result;
   }

   @Override
   protected final String protocolRouteString() {
      return " tag:" + _tag;
   }

   @JsonProperty(ADMINISTRATIVE_COST_VAR)
   public void setAdministrativeCost(int administrativeCost) {
      _administrativeCost = administrativeCost;
   }

}
