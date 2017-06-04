package org.batfish.datamodel;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ Route.DIFF_SYMBOL_VAR })
public class Route implements Comparable<Route>, Serializable {

   private static final String ADMINISTRATIVE_COST_VAR = "administrativeCost";

   public static final String AMBIGUOUS_NEXT_HOP = "(ambiguous)";

   protected static final String DIFF_SYMBOL_VAR = "diffSymbol";

   private static final String METRIC_VAR = "metric";

   private static final String NETWORK_VAR = "network";

   private static final String NEXT_HOP_INTERFACE_VAR = "nextHopInterface";

   private static final String NEXT_HOP_IP_VAR = "nextHopIp";

   private static final String NEXT_HOP_VAR = "nextHop";

   private static final String NODE_VAR = "node";

   private static final String PROTOCOL_VAR = "protocol";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private static final String TAG_VAR = "tag";

   public static final String UNSET_NEXT_HOP = "(unknown)";

   public static final String UNSET_NEXT_HOP_INTERFACE = "dynamic";

   public static final int UNSET_ROUTE_ADMIN = -1;

   public static final int UNSET_ROUTE_COST = -1;

   public static final Ip UNSET_ROUTE_NEXT_HOP_IP = new Ip(-1l);

   public static final int UNSET_ROUTE_TAG = -1;

   private static final String VRF_VAR = "vrf";

   private final int _administrativeCost;

   private final int _metric;

   private final Prefix _network;

   private final transient String _nextHop;

   private final String _nextHopInterface;

   private final Ip _nextHopIp;

   private final String _node;

   private final RoutingProtocol _protocol;

   private final int _tag;

   private final String _vrf;

   @JsonCreator
   public Route(@JsonProperty(NODE_VAR) String node,
         @JsonProperty(VRF_VAR) String vrf,
         @JsonProperty(NETWORK_VAR) Prefix network,
         @JsonProperty(NEXT_HOP_IP_VAR) Ip nextHopIp,
         @JsonProperty(NEXT_HOP_VAR) String nextHop,
         @JsonProperty(NEXT_HOP_INTERFACE_VAR) String nextHopInterface,
         @JsonProperty(ADMINISTRATIVE_COST_VAR) int administrativeCost,
         @JsonProperty(METRIC_VAR) int metric,
         @JsonProperty(PROTOCOL_VAR) RoutingProtocol protocol,
         @JsonProperty(TAG_VAR) int tag) {
      _network = network;
      _nextHopIp = nextHopIp;
      _node = node;
      _nextHop = nextHop;
      _nextHopInterface = nextHopInterface;
      _administrativeCost = administrativeCost;
      _metric = metric;
      _protocol = protocol;
      _tag = tag;
      _vrf = vrf;
   }

   @Override
   public int compareTo(Route rhs) {
      int result = _node.compareTo(rhs._node);
      if (result != 0) {
         return result;
      }
      result = _vrf.compareTo(rhs._vrf);
      if (result != 0) {
         return result;
      }
      result = _network.compareTo(rhs._network);
      if (result != 0) {
         return result;
      }
      result = _nextHopIp.compareTo(rhs._nextHopIp);
      if (result != 0) {
         return result;
      }
      result = Integer.compare(_administrativeCost, rhs._administrativeCost);
      if (result != 0) {
         return result;
      }
      result = Integer.compare(_metric, rhs._metric);
      if (result != 0) {
         return result;
      }
      result = _protocol.compareTo(rhs._protocol);
      if (result != 0) {
         return result;
      }
      result = Integer.compare(_tag, rhs._tag);
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      Route other = (Route) obj;
      if (_administrativeCost != other._administrativeCost) {
         return false;
      }
      if (_metric != other._metric) {
         return false;
      }
      if (!_nextHopIp.equals(other._nextHopIp)) {
         return false;
      }
      if (!_node.equals(other._node)) {
         return false;
      }
      if (!_network.equals(other._network)) {
         return false;
      }
      if (_protocol != other._protocol) {
         return false;
      }
      if (_tag != other._tag) {
         return false;
      }
      if (!_vrf.equals(other._vrf)) {
         return false;
      }
      return true;
   }

   @JsonProperty(ADMINISTRATIVE_COST_VAR)
   public int getAdministrativeCost() {
      return _administrativeCost;
   }

   @JsonProperty(METRIC_VAR)
   public int getMetric() {
      return _metric;
   }

   @JsonProperty(NETWORK_VAR)
   public Prefix getNetwork() {
      return _network;
   }

   @JsonProperty(NEXT_HOP_VAR)
   public String getNextHop() {
      return _nextHop;
   }

   @JsonProperty(NEXT_HOP_INTERFACE_VAR)
   public String getNextHopInterface() {
      return _nextHopInterface;
   }

   @JsonProperty(NEXT_HOP_IP_VAR)
   public Ip getNextHopIp() {
      return _nextHopIp;
   }

   @JsonProperty(NODE_VAR)
   public String getNode() {
      return _node;
   }

   @JsonProperty(PROTOCOL_VAR)
   public RoutingProtocol getProtocol() {
      return _protocol;
   }

   @JsonProperty(TAG_VAR)
   public int getTag() {
      return _tag;
   }

   public String getVrf() {
      return _vrf;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _administrativeCost;
      result = prime * result + _metric;
      result = prime * result + _nextHopIp.hashCode();
      result = prime * result + _node.hashCode();
      result = prime * result + _network.hashCode();
      result = prime * result + _protocol.ordinal();
      result = prime * result + _tag;
      result = prime * result + _vrf.hashCode();
      return result;
   }

   public String prettyPrint(String diffSymbol) {
      String node = getNode();
      String nhnode = getNextHop();
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
      if (!nhint.equals(Route.UNSET_NEXT_HOP_INTERFACE)) {
         // static interface
         if (nextHopIp.equals(Route.UNSET_ROUTE_NEXT_HOP_IP)) {
            nhnode = "N/A";
            nhip = "N/A";
         }
      }
      nhip = nextHopIp != null ? nextHopIp.toString() : "N/A";
      String vrf = getVrf();
      String net = getNetwork().toString();
      String admin = Integer.toString(getAdministrativeCost());
      String cost = Integer.toString(getMetric());
      String prot = getProtocol().protocolName();
      String diffStr = diffSymbol != null ? diffSymbol + " " : "";
      String routeStr = String.format(
            "%s%s vrf:%s net:%s nhip:%s nhint:%s nhnode:%s admin:%s cost:%s tag:%s prot:%s\n",
            diffStr, node, vrf, net, nhip, nhint, nhnode, admin, cost, tag,
            prot);
      return routeStr;
   }

   @Override
   public String toString() {
      String nextHop = _nextHop;
      String nextHopIp = _nextHopIp.toString();
      String tag = Integer.toString(_tag);
      // extra formatting
      if (!_nextHopInterface.equals(UNSET_NEXT_HOP_INTERFACE)) {
         // static interface
         if (_nextHopIp.equals(UNSET_ROUTE_NEXT_HOP_IP)) {
            nextHop = "N/A";
            nextHopIp = "N/A";
         }
      }
      if (_tag == UNSET_ROUTE_TAG) {
         tag = "none";
      }
      return "Route<" + _node.toString() + ", " + _network.toString() + ", "
            + nextHopIp.toString() + ", " + nextHop.toString() + ", "
            + _nextHopInterface.toString() + ", " + _administrativeCost + ", "
            + _metric + ", " + tag + ", " + _protocol.toString() + ">";
   }

}
