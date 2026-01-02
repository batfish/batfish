package org.batfish.datamodel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

public class MockRib implements GenericRib<AnnotatedRoute<AbstractRoute>> {
  private static class DefaultSameRoutePreference
      implements Comparator<AnnotatedRoute<AbstractRoute>>, Serializable {
    private static final DefaultSameRoutePreference INSTANCE = new DefaultSameRoutePreference();

    @Override
    public int compare(AnnotatedRoute<AbstractRoute> o1, AnnotatedRoute<AbstractRoute> o2) {
      return 0;
    }
  }

  public static class Builder {

    private Map<Ip, Set<AnnotatedRoute<AbstractRoute>>> _longestPrefixMatchResults;
    private Set<AnnotatedRoute<AbstractRoute>> _mergeRouteTrues;
    private Comparator<AnnotatedRoute<AbstractRoute>> _routePreferenceComparator;
    private Set<AnnotatedRoute<AbstractRoute>> _routes;
    private Set<AnnotatedRoute<AbstractRoute>> _backupRoutes;

    private Builder() {
      _longestPrefixMatchResults = ImmutableMap.of();
      _mergeRouteTrues = ImmutableSet.of();
      _routePreferenceComparator = DefaultSameRoutePreference.INSTANCE;
      _routes = ImmutableSet.of();
      _backupRoutes = ImmutableSet.of();
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

    public Builder setBackupRoutes(Set<AnnotatedRoute<AbstractRoute>> backupRoutes) {
      _backupRoutes = backupRoutes;
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
  private final Set<AnnotatedRoute<AbstractRoute>> _backupRoutes;

  private MockRib(Builder builder) {
    _longestPrefixMatchResults = builder._longestPrefixMatchResults;
    _mergeRouteTrues = builder._mergeRouteTrues;
    _routePreferenceComparator = builder._routePreferenceComparator;
    _routes = builder._routes;
    _backupRoutes = builder._backupRoutes;
  }

  @Override
  public int comparePreference(
      AnnotatedRoute<AbstractRoute> lhs, AnnotatedRoute<AbstractRoute> rhs) {
    return _routePreferenceComparator.compare(lhs, rhs);
  }

  @Override
  public boolean intersectsPrefixSpace(PrefixSpace prefixSpace) {
    return _routes.stream().map(AnnotatedRoute::getNetwork).anyMatch(prefixSpace::containsPrefix);
  }

  @Override
  public boolean containsRoute(AbstractRouteDecorator route) {
    return _routes.contains(route);
  }

  @Override
  public Set<AbstractRoute> getUnannotatedRoutes() {
    return getRoutes().stream()
        .map(AbstractRouteDecorator::getAbstractRoute)
        .collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public Set<AnnotatedRoute<AbstractRoute>> getRoutes(Prefix prefix) {
    return getRoutes().stream()
        .filter(r -> r.getNetwork().equals(prefix))
        .collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public Set<AnnotatedRoute<AbstractRoute>> getRoutes() {
    return _routes;
  }

  @Override
  public Set<AnnotatedRoute<AbstractRoute>> getBackupRoutes() {
    return _backupRoutes;
  }

  @Override
  public Set<AnnotatedRoute<AbstractRoute>> longestPrefixMatch(
      Ip address, ResolutionRestriction<AnnotatedRoute<AbstractRoute>> restriction) {
    return _longestPrefixMatchResults.getOrDefault(address, ImmutableSet.of());
  }

  @Override
  public Set<AnnotatedRoute<AbstractRoute>> longestPrefixMatch(
      Ip address,
      int maxPrefixLength,
      ResolutionRestriction<AnnotatedRoute<AbstractRoute>> restriction) {
    return _longestPrefixMatchResults.getOrDefault(address, ImmutableSet.of());
  }

  @Override
  public boolean mergeRoute(AnnotatedRoute<AbstractRoute> route) {
    return _mergeRouteTrues.contains(route);
  }
}
