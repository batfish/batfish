package org.batfish.datamodel;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;

public class MockFib implements Fib {

  public static class Builder {

    private Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> _nextHopInterfaces;

    private Map<Ip, Map<String, Map<Ip, Set<AbstractRoute>>>> _nextHopInterfacesByIp;

    private Map<Ip, Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>>>
        _nextHopInterfacesByRoute;

    private Map<String, Set<AbstractRoute>> _routesByNextHopInterface;

    private Builder() {
      _nextHopInterfaces = ImmutableMap.of();
      _nextHopInterfacesByIp = ImmutableMap.of();
      _nextHopInterfacesByRoute = ImmutableMap.of();
      _routesByNextHopInterface = ImmutableMap.of();
    }

    public MockFib build() {
      return new MockFib(this);
    }

    public Builder setNextHopInterfaces(
        Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> nextHopInterfaces) {
      _nextHopInterfaces = nextHopInterfaces;
      return this;
    }

    public Builder setNextHopInterfacesByIp(
        Map<Ip, Map<String, Map<Ip, Set<AbstractRoute>>>> nextHopInterfacesByIp) {
      _nextHopInterfacesByIp = nextHopInterfacesByIp;
      return this;
    }

    public Builder setNextHopInterfacesByRoute(
        Map<Ip, Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>>>
            nextHopInterfacesByRoute) {
      _nextHopInterfacesByRoute = nextHopInterfacesByRoute;
      return this;
    }

    public Builder setRoutesByNextHopInterface(
        Map<String, Set<AbstractRoute>> routesByNextHopInterface) {
      _routesByNextHopInterface = routesByNextHopInterface;
      return this;
    }
  }

  /** */
  private static final long serialVersionUID = 1L;

  public static Builder builder() {
    return new Builder();
  }

  private final Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> _nextHopInterfaces;

  private final Map<Ip, Map<String, Map<Ip, Set<AbstractRoute>>>> _nextHopInterfacesByIp;

  private final Map<Ip, Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>>>
      _nextHopInterfacesByRoute;

  private final Map<String, Set<AbstractRoute>> _routesByNextHopInterface;

  private MockFib(Builder builder) {
    _nextHopInterfaces = ImmutableMap.copyOf(builder._nextHopInterfaces);
    _nextHopInterfacesByIp = ImmutableMap.copyOf(builder._nextHopInterfacesByIp);
    _nextHopInterfacesByRoute = ImmutableMap.copyOf(builder._nextHopInterfacesByRoute);
    _routesByNextHopInterface = ImmutableMap.copyOf(builder._routesByNextHopInterface);
  }

  @Override
  public Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> getNextHopInterfaces() {
    return _nextHopInterfaces;
  }

  @Override
  public Map<String, Map<Ip, Set<AbstractRoute>>> getNextHopInterfaces(Ip ip) {
    return _nextHopInterfacesByIp.get(ip);
  }

  @Override
  public Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> getNextHopInterfacesByRoute(
      Ip dstIp) {
    return _nextHopInterfacesByRoute.get(dstIp);
  }

  @Override
  public Map<String, Set<AbstractRoute>> getRoutesByNextHopInterface() {
    return _routesByNextHopInterface;
  }
}
