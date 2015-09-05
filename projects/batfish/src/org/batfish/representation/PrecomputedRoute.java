package org.batfish.representation;

import java.io.Serializable;

public class PrecomputedRoute implements Comparable<PrecomputedRoute>,
      Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public static final String UNSET_NEXT_HOP = "(unknown)";

   public static final String UNSET_NEXT_HOP_INTERFACE = "dynamic";

   public static final int UNSET_ROUTE_ADMIN = -1;

   public static final int UNSET_ROUTE_COST = -1;

   public static final Ip UNSET_ROUTE_NEXT_HOP_IP = new Ip(-1l);

   public static final int UNSET_ROUTE_TAG = -1;

   private final int _administrativeCost;

   private final int _cost;

   private final Prefix _network;

   private final transient String _nextHop;

   private final String _nextHopInterface;

   private final Ip _nextHopIp;

   private final String _node;

   private final RoutingProtocol _protocol;

   private final int _tag;

   public PrecomputedRoute(String node, Prefix network, Ip nextHopIp,
         String nextHop, String nextHopInterface, int administrativeCost,
         int cost, RoutingProtocol protocol, int tag) {
      _node = node;
      _network = network;
      _nextHopIp = nextHopIp;
      _nextHop = nextHop;
      _nextHopInterface = nextHopInterface;
      _administrativeCost = administrativeCost;
      _cost = cost;
      _protocol = protocol;
      _tag = tag;
   }

   @Override
   public int compareTo(PrecomputedRoute rhs) {
      int result = _node.compareTo(rhs._node);
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
      result = Integer.compare(_cost, rhs._cost);
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
      PrecomputedRoute other = (PrecomputedRoute) obj;
      if (_administrativeCost != other._administrativeCost) {
         return false;
      }
      if (_cost != other._cost) {
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
      return true;
   }

   public int getAdministrativeCost() {
      return _administrativeCost;
   }

   public int getCost() {
      return _cost;
   }

   public String getNextHop() {
      return _nextHop;
   }

   public String getNextHopInterface() {
      return _nextHopInterface;
   }

   public Ip getNextHopIp() {
      return _nextHopIp;
   }

   public String getNode() {
      return _node;
   }

   public Prefix getPrefix() {
      return _network;
   }

   public RoutingProtocol getProtocol() {
      return _protocol;
   }

   public int getTag() {
      return _tag;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _administrativeCost;
      result = prime * result + _cost;
      result = prime * result + _nextHopIp.hashCode();
      result = prime * result + _node.hashCode();
      result = prime * result + _network.hashCode();
      result = prime * result + _protocol.hashCode();
      result = prime * result + _tag;
      return result;
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
            + _cost + ", " + tag + ", " + _protocol.toString() + ">";
   }

}
