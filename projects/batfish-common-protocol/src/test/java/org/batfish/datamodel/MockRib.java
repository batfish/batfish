package org.batfish.datamodel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

public class MockRib implements GenericRib<AbstractRoute> {

  public static class Builder {

    private Map<Ip, Set<AbstractRoute>> _longestPrefixMatchResults;
    private Set<AbstractRoute> _mergeRouteTrues;
    private Comparator<AbstractRoute> _routePreferenceComparator;
    private Set<AbstractRoute> _routes;

    private Builder() {
      _longestPrefixMatchResults = ImmutableMap.of();
      _mergeRouteTrues = ImmutableSet.of();
      _routePreferenceComparator = (a, b) -> 0;
      _routes = ImmutableSet.of();
    }

    public MockRib build() {
      return new MockRib(this);
    }

    public Builder setLongestPrefixMatchResults(
        Map<Ip, Set<AbstractRoute>> longestPrefixMatchResults) {
      _longestPrefixMatchResults = longestPrefixMatchResults;
      return this;
    }

    public Builder setMergeRouteTrues(Set<AbstractRoute> mergeRouteTrues) {
      _mergeRouteTrues = mergeRouteTrues;
      return this;
    }

    public Builder setRoutePreferenceComparator(
        Comparator<AbstractRoute> routePreferenceComparator) {
      _routePreferenceComparator = routePreferenceComparator;
      return this;
    }

    public Builder setRoutes(Set<AbstractRoute> routes) {
      _routes = routes;
      return this;
    }
  }

  private static final long serialVersionUID = 1L;

  public static Builder builder() {
    return new Builder();
  }

  private final Map<Ip, Set<AbstractRoute>> _longestPrefixMatchResults;
  private final Set<AbstractRoute> _mergeRouteTrues;
  private final Comparator<AbstractRoute> _routePreferenceComparator;
  private final Set<AbstractRoute> _routes;

  private MockRib(Builder builder) {
    _longestPrefixMatchResults = builder._longestPrefixMatchResults;
    _mergeRouteTrues = builder._mergeRouteTrues;
    _routePreferenceComparator = builder._routePreferenceComparator;
    _routes = builder._routes;
  }

  @Override
  public int comparePreference(AbstractRoute lhs, AbstractRoute rhs) {
    return _routePreferenceComparator.compare(lhs, rhs);
  }

  @Override
  public Set<AbstractRoute> getRoutes() {
    return _routes;
  }

  @Override
  public Set<AbstractRoute> getTypedRoutes() {
    return _routes;
  }

  @Override
  public Set<AbstractRoute> longestPrefixMatch(Ip address) {
    return _longestPrefixMatchResults.get(address);
  }

  @Override
  public Set<AbstractRoute> longestPrefixMatch(Ip address, int maxPrefixLength) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean mergeRoute(AbstractRoute route) {
    return _mergeRouteTrues.contains(route);
  }
}
