package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public class MockFib implements Fib {

  public static class Builder {
    private Map<Prefix, IpSpace> _matchingIps;

    private Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> _nextHopInterfaces;

    private Map<Ip, Map<String, Map<Ip, Set<AbstractRoute>>>> _nextHopInterfacesByIp;

    private Map<Ip, Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>>>
        _nextHopInterfacesByRoute;

    private Map<String, Set<AbstractRoute>> _routesByNextHopInterface;

    private Map<Ip, Set<FibEntry>> _fibEntries;

    private Builder() {
      _matchingIps = ImmutableMap.of();
      _nextHopInterfaces = ImmutableMap.of();
      _nextHopInterfacesByIp = ImmutableMap.of();
      _nextHopInterfacesByRoute = ImmutableMap.of();
      _routesByNextHopInterface = ImmutableMap.of();
      _fibEntries = ImmutableMap.of();
    }

    public MockFib build() {
      return new MockFib(this);
    }

    public Builder setMatchingIps(@Nonnull Map<Prefix, IpSpace> matchingIps) {
      _matchingIps = matchingIps;
      return this;
    }

    public Builder setNextHopInterfaces(
        @Nonnull Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> nextHopInterfaces) {
      _nextHopInterfaces = nextHopInterfaces;
      return this;
    }

    @Deprecated
    public Builder setNextHopInterfacesByIp(
        @Nonnull Map<Ip, Map<String, Map<Ip, Set<AbstractRoute>>>> nextHopInterfacesByIp) {
      _nextHopInterfacesByIp = nextHopInterfacesByIp;
      return this;
    }

    @Deprecated
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

    public Builder setFibEntries(@Nonnull Map<Ip, Set<FibEntry>> fibEntries) {
      _fibEntries = fibEntries;
      return this;
    }
  }

  /** */
  private static final long serialVersionUID = 1L;

  public static Builder builder() {
    return new Builder();
  }

  private final Map<Prefix, IpSpace> _matchingIps;

  private final Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> _nextHopInterfaces;

  private final Map<Ip, Map<String, Map<Ip, Set<AbstractRoute>>>> _nextHopInterfacesByIp;

  private final Map<Ip, Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>>>
      _nextHopInterfacesByRoute;

  private final Map<String, Set<AbstractRoute>> _routesByNextHopInterface;

  private Map<Ip, Set<FibEntry>> _fibEntries;

  private MockFib(Builder builder) {
    _matchingIps = ImmutableMap.copyOf(builder._matchingIps);
    _nextHopInterfaces = ImmutableMap.copyOf(builder._nextHopInterfaces);
    _nextHopInterfacesByIp = ImmutableMap.copyOf(builder._nextHopInterfacesByIp);
    _nextHopInterfacesByRoute = ImmutableMap.copyOf(builder._nextHopInterfacesByRoute);
    _routesByNextHopInterface = ImmutableMap.copyOf(builder._routesByNextHopInterface);
    _fibEntries = ImmutableMap.copyOf(builder._fibEntries);
  }

  @Nonnull
  @Override
  public Set<FibEntry> allEntries() {
    return _fibEntries.values().stream()
        .flatMap(Set::stream)
        .collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public @Nonnull Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>>
      getNextHopInterfaces() {
    return _nextHopInterfaces;
  }

  @Override
  @Deprecated
  public @Nonnull Set<String> getNextHopInterfaces(Ip ip) {
    return _nextHopInterfacesByIp.getOrDefault(ip, ImmutableMap.of()).keySet();
  }

  @Nonnull
  @Override
  public Set<FibEntry> get(Ip ip) {
    return firstNonNull(_fibEntries.get(ip), ImmutableSet.of());
  }

  @Override
  @Deprecated
  public @Nonnull Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>>
      getNextHopInterfacesByRoute(Ip dstIp) {
    return _nextHopInterfacesByRoute.get(dstIp);
  }

  @Override
  public @Nonnull Map<String, Set<AbstractRoute>> getRoutesByNextHopInterface() {
    return _routesByNextHopInterface;
  }

  @Nonnull
  @Override
  public Map<Prefix, IpSpace> getMatchingIps() {
    return _matchingIps;
  }
}
