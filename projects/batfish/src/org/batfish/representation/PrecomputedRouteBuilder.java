package org.batfish.representation;

public class PrecomputedRouteBuilder {

   private int _administrativeCost;

   private int _cost;

   private Prefix _network;

   private String _nextHop;

   private String _nextHopInterface;

   private Ip _nextHopIp;

   private String _node;

   private RoutingProtocol _protocol;

   private int _tag;

   public PrecomputedRouteBuilder() {
      _administrativeCost = PrecomputedRoute.UNSET_ROUTE_ADMIN;
      _cost = PrecomputedRoute.UNSET_ROUTE_COST;
      _tag = PrecomputedRoute.UNSET_ROUTE_TAG;
      _nextHopInterface = PrecomputedRoute.UNSET_NEXT_HOP_INTERFACE;
      _nextHopIp = PrecomputedRoute.UNSET_ROUTE_NEXT_HOP_IP;
      _nextHop = PrecomputedRoute.UNSET_NEXT_HOP;
   }

   public PrecomputedRoute build() {
      return new PrecomputedRoute(_node, _network, _nextHopIp, _nextHop,
            _nextHopInterface, _administrativeCost, _cost, _protocol, _tag);
   }

   public void setAdministrativeCost(int administrativeCost) {
      _administrativeCost = administrativeCost;
   }

   public void setCost(int cost) {
      _cost = cost;
   }

   public void setNetwork(Prefix network) {
      _network = network;
   }

   public void setNextHop(String nextHop) {
      _nextHop = nextHop;
   }

   public void setNextHopInterface(String nextHopInterface) {
      _nextHopInterface = nextHopInterface;
   }

   public void setNextHopIp(Ip nextHopIp) {
      _nextHopIp = nextHopIp;
   }

   public void setNode(String node) {
      _node = node;
   }

   public void setProtocol(RoutingProtocol protocol) {
      _protocol = protocol;
   }

   public void setTag(int tag) {
      _tag = tag;
   }

}
