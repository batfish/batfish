package batfish.z3;

import java.util.List;

import batfish.representation.Ip;

public class PolicyRouteNextHopInterfaceEntry {
   
   private final String _node;
   private final Ip _nextHopIp;
   private final List<String> _nextHopInterfaces;
   
   public PolicyRouteNextHopInterfaceEntry(String node, Ip nextHopIp, List<String> nextHopInterfaces) {
      _node = node;
      _nextHopIp = nextHopIp;
      _nextHopInterfaces = nextHopInterfaces;
   }
   
   public String getNode() {
      return _node;
   }
   
   public Ip getNextHopIp() {
      return _nextHopIp;
   }
   
   public List<String> getNextHopInterfaces() {
      return _nextHopInterfaces;
   }
   
}
