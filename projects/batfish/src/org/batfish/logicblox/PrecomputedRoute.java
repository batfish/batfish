package org.batfish.logicblox;

import java.io.Serializable;

import org.batfish.representation.Ip;
import org.batfish.representation.Prefix;
import org.batfish.representation.RoutingProtocol;

public class PrecomputedRoute implements Comparable<PrecomputedRoute>,
      Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _administrativeCost;

   private final int _cost;

   private final Ip _nextHopIp;

   private final String _node;

   private final Prefix _prefix;

   private final RoutingProtocol _protocol;

   private final int _tag;

   public PrecomputedRoute(String node, Prefix prefix, Ip nextHopIp,
         int administrativeCost, int cost, RoutingProtocol protocol, int tag) {
      _node = node;
      _prefix = prefix;
      _nextHopIp = nextHopIp;
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
      result = _prefix.compareTo(rhs._prefix);
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
      if (!_prefix.equals(other._prefix)) {
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

   public Ip getNextHopIp() {
      return _nextHopIp;
   }

   public String getNode() {
      return _node;
   }

   public Prefix getPrefix() {
      return _prefix;
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
      result = prime * result + _prefix.hashCode();
      result = prime * result + _protocol.hashCode();
      result = prime * result + _tag;
      return result;
   }

}
