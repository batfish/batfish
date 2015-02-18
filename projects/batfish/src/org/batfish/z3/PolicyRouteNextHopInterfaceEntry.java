package org.batfish.z3;

import java.util.List;

import org.batfish.representation.Ip;

public class PolicyRouteNextHopInterfaceEntry {

   private final List<String> _nextHopInterfaces;
   private final Ip _nextHopIp;
   private final String _node;

   public PolicyRouteNextHopInterfaceEntry(String node, Ip nextHopIp,
         List<String> nextHopInterfaces) {
      _node = node;
      _nextHopIp = nextHopIp;
      _nextHopInterfaces = nextHopInterfaces;
   }

   public List<String> getNextHopInterfaces() {
      return _nextHopInterfaces;
   }

   public Ip getNextHopIp() {
      return _nextHopIp;
   }

   public String getNode() {
      return _node;
   }

}
