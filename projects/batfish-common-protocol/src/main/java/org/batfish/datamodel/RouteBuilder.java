package org.batfish.datamodel;

public class RouteBuilder {

  private int _administrativeCost;

  private long _cost;

  private Prefix _network;

  private String _nextHop;

  private String _nextHopInterface;

  private Ip _nextHopIp;

  private String _node;

  private RoutingProtocol _protocol;

  private int _tag;

  private String _vrf;

  public RouteBuilder() {
    _administrativeCost = Route.UNSET_ROUTE_ADMIN;
    _cost = Route.UNSET_ROUTE_COST;
    _tag = Route.UNSET_ROUTE_TAG;
    _nextHopInterface = Route.UNSET_NEXT_HOP_INTERFACE;
    _nextHopIp = Route.UNSET_ROUTE_NEXT_HOP_IP;
    _nextHop = Route.UNSET_NEXT_HOP;
  }

  public Route build() {
    return new Route(
        _node,
        _vrf,
        _network,
        _nextHopIp,
        _nextHop,
        _nextHopInterface,
        _administrativeCost,
        _cost,
        _protocol,
        _tag);
  }

  public void setAdministrativeCost(int administrativeCost) {
    _administrativeCost = administrativeCost;
  }

  public void setCost(long cost) {
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

  public void setVrf(String vrf) {
    _vrf = vrf;
  }
}
