package org.batfish.datamodel;

import java.io.Serializable;
import java.util.Map;

import org.batfish.common.BatfishException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public abstract class AbstractRoute
      implements Serializable, Comparable<AbstractRoute> {

   protected static final String ADMINISTRATIVE_COST_VAR = "administrativeCost";

   private static final String METRIC_VAR = "metric";

   protected static final String NETWORK_VAR = "network";

   protected static final String NEXT_HOP_IP_VAR = "nextHopIp";

   public static final int NO_TAG = -1;

   private static final long serialVersionUID = 1L;

   protected final Prefix _network;

   private String _nextHop;

   protected final Ip _nextHopIp;

   private String _node;

   private boolean _nonRouting;

   private String _vrf;

   public AbstractRoute(Prefix network, Ip nextHopIp) {
      if (network == null) {
         throw new BatfishException(
               "Cannot construct AbstractRoute with null network");
      }
      _network = network;
      _nextHopIp = nextHopIp;
   }

   @Override
   public final int compareTo(AbstractRoute rhs) {
      int ret;
      ret = _network.compareTo(rhs._network);
      if (ret != 0) {
         return ret;
      }
      ret = Integer.compare(getAdministrativeCost(),
            rhs.getAdministrativeCost());
      if (ret != 0) {
         return ret;
      }
      ret = Integer.compare(getMetric(), rhs.getMetric());
      if (ret != 0) {
         return ret;
      }
      ret = routeCompare(rhs);
      if (ret != 0) {
         return ret;
      }
      if (_nextHopIp == null) {
         if (rhs._nextHopIp != null) {
            ret = -1;
         }
         else {
            ret = 0;
         }
      }
      else {
         ret = _nextHopIp.compareTo(rhs._nextHopIp);
      }
      if (ret != 0) {
         return ret;
      }
      String nextHopInterface = getNextHopInterface();
      String rhsNextHopInterface = rhs.getNextHopInterface();
      if (nextHopInterface == null) {
         if (rhsNextHopInterface != null) {
            ret = -1;
         }
         else {
            ret = 0;
         }
      }
      else {
         ret = nextHopInterface.compareTo(rhsNextHopInterface);
      }
      if (ret != 0) {
         return ret;
      }
      ret = Integer.compare(getTag(), rhs.getTag());
      return ret;
   }

   @Override
   public abstract boolean equals(Object o);

   public final String fullString() {
      String nhnode = _nextHop;
      Ip nextHopIp = getNextHopIp();
      String nhip;
      String tag;
      int tagInt = getTag();
      if (tagInt == Route.UNSET_ROUTE_TAG) {
         tag = "none";
      }
      else {
         tag = Integer.toString(tagInt);
      }
      String nhint = getNextHopInterface();
      if (nhint != null && !nhint.equals(Route.UNSET_NEXT_HOP_INTERFACE)) {
         // static interface
         if (nextHopIp.equals(Route.UNSET_ROUTE_NEXT_HOP_IP)) {
            nhnode = "N/A";
            nhip = "N/A";
         }
      }
      nhip = nextHopIp != null ? nextHopIp.toString() : "N/A";
      String net = getNetwork().toString();
      String admin = Integer.toString(getAdministrativeCost());
      String cost = Integer.toString(getMetric());
      String prot = getProtocol().protocolName();
      String routeStr = String.format(
            "%s vrf:%s net:%s nhip:%s nhint:%s nhnode:%s admin:%s cost:%s tag:%s prot:%s %s",
            _node, _vrf, net, nhip, nhint, nhnode, admin, cost, tag, prot,
            protocolRouteString());
      return routeStr;
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

   public String getNextHop() {
      return _nextHop;
   }

   @JsonPropertyDescription("The explicit next-hop interface for this route")
   public abstract String getNextHopInterface();

   @JsonProperty(NEXT_HOP_IP_VAR)
   @JsonPropertyDescription("The IPV4 address of the next-hop router for this route")
   public Ip getNextHopIp() {
      return _nextHopIp;
   }

   public String getNode() {
      return _node;
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

   public String getVrf() {
      return _vrf;
   }

   @Override
   public abstract int hashCode();

   protected abstract String protocolRouteString();

   public abstract int routeCompare(AbstractRoute rhs);

   public void setNextHop(String nextHop) {
      _nextHop = nextHop;
   }

   public void setNode(String node) {
      _node = node;
   }

   @JsonIgnore
   public final void setNonRouting(boolean nonRouting) {
      _nonRouting = nonRouting;
   }

   public void setVrf(String vrf) {
      _vrf = vrf;
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + "<" + _network.toString()
            + ",nhip:" + _nextHopIp + ",nhint:" + getNextHopInterface() + ">";
   }

   public Route toSummaryRoute(String hostname, String vrfName,
         Map<Ip, String> ipOwners) {
      RouteBuilder rb = new RouteBuilder();
      rb.setNode(hostname);
      rb.setNetwork(getNetwork());
      Ip nextHopIp = getNextHopIp();
      if (getProtocol() == RoutingProtocol.CONNECTED
            || (getProtocol() == RoutingProtocol.STATIC
                  && nextHopIp.equals(Route.UNSET_ROUTE_NEXT_HOP_IP))
            || Interface.NULL_INTERFACE_NAME.equals(getNextHopInterface())) {
         rb.setNextHop(Configuration.NODE_NONE_NAME);
      }
      if (!nextHopIp.equals(Route.UNSET_ROUTE_NEXT_HOP_IP)) {
         rb.setNextHopIp(nextHopIp);
         String nextHop = ipOwners.get(nextHopIp);
         if (nextHop != null) {
            rb.setNextHop(nextHop);
         }
      }
      String nextHopInterface = getNextHopInterface();
      if (nextHopInterface != null) {
         rb.setNextHopInterface(nextHopInterface);
      }
      rb.setAdministrativeCost(getAdministrativeCost());
      rb.setCost(getMetric());
      rb.setProtocol(getProtocol());
      rb.setTag(getTag());
      rb.setVrf(vrfName);
      Route outputRoute = rb.build();
      return outputRoute;
   }

}
