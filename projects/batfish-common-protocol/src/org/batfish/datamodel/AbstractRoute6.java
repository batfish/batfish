package org.batfish.datamodel;

import java.io.Serializable;

import org.batfish.common.BatfishException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "class")
public abstract class AbstractRoute6 implements Serializable {

   protected static final String ADMINISTRATIVE_COST_VAR = "administrativeCost";

   private static final String METRIC_VAR = "metric";

   protected static final String NETWORK_VAR = "network";

   protected static final String NEXT_HOP_IP_VAR = "nextHopIp";

   public static final int NO_TAG = -1;

   private static final long serialVersionUID = 1L;

   protected final Prefix6 _network;

   protected final Ip6 _nextHopIp;

   private boolean _nonRouting;

   public AbstractRoute6(Prefix6 network, Ip6 nextHopIp) {
      if (network == null) {
         throw new BatfishException(
               "Cannot construct AbstractRoute with null network");
      }
      _network = network;
      _nextHopIp = nextHopIp;
   }

   @Override
   public abstract boolean equals(Object o);

   @JsonProperty(ADMINISTRATIVE_COST_VAR)
   public abstract int getAdministrativeCost();

   @JsonProperty(METRIC_VAR)
   public abstract Integer getMetric();

   @JsonProperty(NETWORK_VAR)
   public final Prefix6 getNetwork() {
      return _network;
   }

   public abstract String getNextHopInterface();

   @JsonProperty(NEXT_HOP_IP_VAR)
   public Ip6 getNextHopIp() {
      return _nextHopIp;
   }

   @JsonIgnore
   public final boolean getNonRouting() {
      return _nonRouting;
   }

   @JsonIgnore
   public abstract RoutingProtocol getProtocol();

   public abstract int getTag();

   @Override
   public abstract int hashCode();

   @JsonIgnore
   public final void setNonRouting(boolean nonRouting) {
      _nonRouting = nonRouting;
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + "<" + _network.toString() + ">";
   }

}
