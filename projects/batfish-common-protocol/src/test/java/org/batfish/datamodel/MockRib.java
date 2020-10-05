package org.batfish.datamodel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

public class MockRib implements GenericRib<AnnotatedRoute<AbstractRoute>> {

  public static class Builder {

    private Map<Ip, Set<AnnotatedRoute<AbstractRoute>>> _longestPrefixMatchResults;
    private Set<AnnotatedRoute<AbstractRoute>> _mergeRouteTrues;
    private Comparator<AnnotatedRoute<AbstractRoute>> _routePreferenceComparator;
    private Set<AnnotatedRoute<AbstractRoute>> _routes;

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
        Map<Ip, Set<AnnotatedRoute<AbstractRoute>>> longestPrefixMatchResults) {
      _longestPrefixMatchResults = longestPrefixMatchResults;
      return this;
    }

    public Builder setMergeRouteTrues(Set<AnnotatedRoute<AbstractRoute>> mergeRouteTrues) {
      _mergeRouteTrues = mergeRouteTrues;
      return this;
    }

    public Builder setRoutePreferenceComparator(
        Comparator<AnnotatedRoute<AbstractRoute>> routePreferenceComparator) {
      _routePreferenceComparator = routePreferenceComparator;
      return this;
    }

    public Builder setRoutes(Set<AnnotatedRoute<AbstractRoute>> routes) {
      _routes = routes;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private final Map<Ip, Set<AnnotatedRoute<AbstractRoute>>> _longestPrefixMatchResults;
  private final Set<AnnotatedRoute<AbstractRoute>> _mergeRouteTrues;
  private final Comparator<AnnotatedRoute<AbstractRoute>> _routePreferenceComparator;
  private final Set<AnnotatedRoute<AbstractRoute>> _routes;

  private MockRib(Builder builder) {
    _longestPrefixMatchResults = builder._longestPrefixMatchResults;
    _mergeRouteTrues = builder._mergeRouteTrues;
    _routePreferenceComparator = builder._routePreferenceComparator;
    _routes = builder._routes;
  }

  @Override
  public int comparePreference(
      AnnotatedRoute<AbstractRoute> lhs, AnnotatedRoute<AbstractRoute> rhs) {
    return _routePreferenceComparator.compare(lhs, rhs);
  }

  @Override
  public boolean containsRoute(AbstractRouteDecorator route) {
    return _routes.contains(route);
  }

  @Override
  public Set<AbstractRoute> getRoutes() {
    return getTypedRoutes().stream()
        .map(AbstractRouteDecorator::getAbstractRoute)
        .collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public Set<AnnotatedRoute<AbstractRoute>> getTypedRoutes() {
    return _routes;
  }

  @Override
  public Set<AnnotatedRoute<AbstractRoute>> longestPrefixMatch(Ip address) {
    return _longestPrefixMatchResults.get(address);
  }

  @Override
  public Set<AnnotatedRoute<AbstractRoute>> longestPrefixMatch(Ip address, int maxPrefixLength) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean mergeRoute(AnnotatedRoute<AbstractRoute> route) {
    return _mergeRouteTrues.contains(route);
  }
}
