package org.batfish.datamodel;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

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
        @Nonnull Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> nextHopInterfaces) {
      _nextHopInterfaces = nextHopInterfaces;
      return this;
    }

    public Builder setNextHopInterfacesByIp(
        @Nonnull Map<Ip, Map<String, Map<Ip, Set<AbstractRoute>>>> nextHopInterfacesByIp) {
      _nextHopInterfacesByIp = nextHopInterfacesByIp;
      return this;
    }

    public Builder setNextHopInterfacesByRoute(
        @Nonnull
            Map<Ip, Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>>>
                nextHopInterfacesByRoute) {
      _nextHopInterfacesByRoute = nextHopInterfacesByRoute;
      return this;
    }

    public Builder setRoutesByNextHopInterface(
        @Nonnull Map<String, Set<AbstractRoute>> routesByNextHopInterface) {
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
  public @Nonnull Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>>
      getNextHopInterfaces() {
    return _nextHopInterfaces;
  }

  @Override
  public @Nonnull Set<String> getNextHopInterfaces(Ip ip) {
    return _nextHopInterfacesByIp.getOrDefault(ip, ImmutableMap.of()).keySet();
  }

  @Override
  public @Nonnull Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>>
      getNextHopInterfacesByRoute(Ip dstIp) {
    return _nextHopInterfacesByRoute.get(dstIp);
  }

  @Override
  public @Nonnull Map<String, Set<AbstractRoute>> getRoutesByNextHopInterface() {
    return _routesByNextHopInterface;
  }
}
