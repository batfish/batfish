package org.batfish.datamodel;

import java.io.Serializable;

import org.batfish.common.BatfishException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public abstract class AbstractRoute implements Serializable {

   protected static final String ADMINISTRATIVE_COST_VAR = "administrativeCost";

   private static final String METRIC_VAR = "metric";

   protected static final String NETWORK_VAR = "network";

   protected static final String NEXT_HOP_IP_VAR = "nextHopIp";

   public static final int NO_TAG = -1;

   private static final long serialVersionUID = 1L;

   protected final Prefix _network;

   protected final Ip _nextHopIp;

   private boolean _nonRouting;

   public AbstractRoute(Prefix network, Ip nextHopIp) {
      if (network == null) {
         throw new BatfishException(
               "Cannot construct AbstractRoute with null network");
      }
      _network = network;
      _nextHopIp = nextHopIp;
   }

   @Override
   public abstract boolean equals(Object o);

   public final String fullString() {
      return this.getClass().getSimpleName() + "<" + _network.toString()
            + " nhip:" + _nextHopIp + " nhint:" + getNextHopInterface()
            + protocolRouteString() + ">";
   }

   @JsonProperty(ADMINISTRATIVE_COST_VAR)
   @JsonPropertyDescription("Administrative cost for this route (usually based on protocol)")
   public abstract int getAdministrativeCost();

   @JsonProperty(METRIC_VAR)
   @JsonPropertyDescription("Protocol-specific cost for this route")
   public abstract Integer getMetric();

   @JsonProperty(NETWORK_VAR)
   @JsonPropertyDescription("IPV4 network of this route")
   public final Prefix getNetwork() {
      return _network;
   }

   @JsonPropertyDescription("The explicit next-hop interface for this route")
   public abstract String getNextHopInterface();

   @JsonProperty(NEXT_HOP_IP_VAR)
   @JsonPropertyDescription("The IPV4 address of the next-hop router for this route")
   public Ip getNextHopIp() {
      return _nextHopIp;
   }

   @JsonIgnore
   public final boolean getNonRouting() {
      return _nonRouting;
   }

   @JsonIgnore
   @JsonPropertyDescription("The routing protocol that produced this route")
   public abstract RoutingProtocol getProtocol();

   @JsonPropertyDescription("The non-transitive tag attribute of this route")
   public abstract int getTag();

   @Override
   public abstract int hashCode();

   protected abstract String protocolRouteString();

   @JsonIgnore
   public final void setNonRouting(boolean nonRouting) {
      _nonRouting = nonRouting;
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + "<" + _network.toString()
            + ",nhip:" + _nextHopIp + ",nhint:" + getNextHopInterface() + ">";
   }

}
