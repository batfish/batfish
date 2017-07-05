package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.util.Map;
import org.batfish.common.BatfishException;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public abstract class AbstractRoute
      implements Serializable, Comparable<AbstractRoute> {

   protected static final String ADMINISTRATIVE_COST_VAR = "administrativeCost";

   protected static final String METRIC_VAR = "metric";

   protected static final String NETWORK_VAR = "network";

   protected static final String NEXT_HOP_INTERFACE_VAR = "nextHopInterface";

   protected static final String NEXT_HOP_IP_VAR = "nextHopIp";

   private static final String NEXT_HOP_VAR = "nextHop";

   public static final int NO_TAG = -1;

   private static final String NODE_VAR = "node";

   protected static final String PROTOCOL_VAR = "protocol";

   private static final long serialVersionUID = 1L;

   protected static final String TAG_VAR = "tag";

   private static final String VRF_VAR = "vrf";

   protected final Prefix _network;

   private String _nextHop;

   private String _node;

   private boolean _nonRouting;

   private String _vrf;

   @JsonCreator
   public AbstractRoute(@JsonProperty(NETWORK_VAR) Prefix network) {
      if (network == null) {
         throw new BatfishException(
               "Cannot construct AbstractRoute with null network");
      }
      _network = network;
   }

   @Override
   public final int compareTo(AbstractRoute rhs) {
      int ret;
      ret = _network.compareTo(rhs._network);
      if (ret != 0) {
         return ret;
      }
      ret = Integer.compare(
            getAdministrativeCost(),
            rhs.getAdministrativeCost());
      if (ret != 0) {
         return ret;
      }
      Integer lhsMetric = getMetric();
      Integer rhsMetric = rhs.getMetric();
      if (lhsMetric == null) {
         if (rhsMetric != null) {
            ret = -1;
         }
         else {
            ret = 0;
         }
      }
      else if (rhsMetric == null) {
         ret = 1;
      }
      else {
         ret = Integer.compare(lhsMetric, rhsMetric);
      }
      if (ret != 0) {
         return ret;
      }
      ret = routeCompare(rhs);
      if (ret != 0) {
         return ret;
      }
      Ip lhsNextHopIp = getNextHopIp();
      Ip rhsNextHopIp = rhs.getNextHopIp();
      if (lhsNextHopIp == null) {
         if (rhsNextHopIp != null) {
            ret = -1;
         }
         else {
            ret = 0;
         }
      }
      else if (rhsNextHopIp == null) {
         ret = 1;
      }
      else {
         ret = lhsNextHopIp.compareTo(rhsNextHopIp);
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

   @JsonIgnore
   public abstract int getAdministrativeCost();

   @JsonIgnore
   public abstract Integer getMetric();

   @JsonProperty(NETWORK_VAR)
   @JsonPropertyDescription("IPV4 network of this route")
   public final Prefix getNetwork() {
      return _network;
   }

   @JsonProperty(NEXT_HOP_VAR)
   public String getNextHop() {
      return _nextHop;
   }

   @JsonIgnore
   public abstract String getNextHopInterface();

   @JsonIgnore
   public abstract Ip getNextHopIp();

   @JsonProperty(NODE_VAR)
   public String getNode() {
      return _node;
   }

   @JsonIgnore
   public final boolean getNonRouting() {
      return _nonRouting;
   }

   @JsonIgnore
   public abstract RoutingProtocol getProtocol();

   @JsonIgnore
   public abstract int getTag();

   @JsonProperty(VRF_VAR)
   public String getVrf() {
      return _vrf;
   }

   @Override
   public abstract int hashCode();

   protected abstract String protocolRouteString();

   public abstract int routeCompare(AbstractRoute rhs);

   @JsonProperty(NEXT_HOP_VAR)
   public void setNextHop(String nextHop) {
      _nextHop = nextHop;
   }

   @JsonProperty(NODE_VAR)
   public void setNode(String node) {
      _node = node;
   }

   @JsonIgnore
   public final void setNonRouting(boolean nonRouting) {
      _nonRouting = nonRouting;
   }

   @JsonProperty(VRF_VAR)
   public void setVrf(String vrf) {
      _vrf = vrf;
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + "<" + _network.toString()
            + ",nhip:" + getNextHopIp() + ",nhint:" + getNextHopInterface()
            + ">";
   }

   public Route toSummaryRoute(
         String hostname, String vrfName,
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
