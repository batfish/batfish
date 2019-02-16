package org.batfish.datamodel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public class MockRib implements GenericRib<HasAbstractRoute> {

  public static class Builder {

    private Map<Ip, Set<HasAbstractRoute>> _longestPrefixMatchResults;

    private Map<Prefix, IpSpace> _matchingIps;

    private Set<HasAbstractRoute> _mergeRouteTrues;

    private SortedSet<Prefix> _prefixes;

    private IpSpace _routableIps;

    private Comparator<AbstractRoute> _routePreferenceComparator;

    private Set<HasAbstractRoute> _routes;

    private Builder() {
      _longestPrefixMatchResults = ImmutableMap.of();
      _matchingIps = ImmutableMap.of();
      _mergeRouteTrues = ImmutableSet.of();
      _routePreferenceComparator = Comparator.naturalOrder();
      _routes = ImmutableSet.of();
    }

    public MockRib build() {
      return new MockRib(this);
    }

    public Builder setLongestPrefixMatchResults(
        Map<Ip, Set<HasAbstractRoute>> longestPrefixMatchResults) {
      _longestPrefixMatchResults = longestPrefixMatchResults;
      return this;
    }

    public Builder setMatchingIps(Map<Prefix, IpSpace> matchingIps) {
      _matchingIps = matchingIps;
      return this;
    }

    public Builder setMergeRouteTrues(Set<HasAbstractRoute> mergeRouteTrues) {
      _mergeRouteTrues = mergeRouteTrues;
      return this;
    }

    public Builder setPrefixes(SortedSet<Prefix> prefixes) {
      _prefixes = prefixes;
      return this;
    }

    public Builder setRoutableIps(IpSpace routableIps) {
      _routableIps = routableIps;
      return this;
    }

    public Builder setRoutePreferenceComparator(
        Comparator<AbstractRoute> routePreferenceComparator) {
      _routePreferenceComparator = routePreferenceComparator;
      return this;
    }

    public Builder setRoutes(Set<HasAbstractRoute> routes) {
      _routes = routes;
      return this;
    }
  }

  /** */
  private static final long serialVersionUID = 1L;

  public static Builder builder() {
    return new Builder();
  }

  private final Map<Ip, Set<HasAbstractRoute>> _longestPrefixMatchResults;

  private final Map<Prefix, IpSpace> _matchingIps;

  private final Set<HasAbstractRoute> _mergeRouteTrues;

  private final SortedSet<Prefix> _prefixes;

  private final IpSpace _routableIps;

  private final Comparator<AbstractRoute> _routePreferenceComparator;

  private final Set<HasAbstractRoute> _routes;

  private MockRib(Builder builder) {
    _longestPrefixMatchResults = builder._longestPrefixMatchResults;
    _matchingIps = builder._matchingIps;
    _mergeRouteTrues = builder._mergeRouteTrues;
    _prefixes = builder._prefixes;
    _routableIps = builder._routableIps;
    _routePreferenceComparator = builder._routePreferenceComparator;
    _routes = builder._routes;
  }

  @Override
  public int comparePreference(HasAbstractRoute lhs, HasAbstractRoute rhs) {
    return _routePreferenceComparator.compare(lhs.getAbstractRoute(), rhs.getAbstractRoute());
  }

  @Override
  public Map<Prefix, IpSpace> getMatchingIps() {
    return _matchingIps;
  }

  @Override
  public SortedSet<Prefix> getPrefixes() {
    return _prefixes;
  }

  @Override
  public IpSpace getRoutableIps() {
    return _routableIps;
  }

  @Override
  public Set<HasAbstractRoute> getRoutes() {
    return _routes;
  }

  @Override
  public Set<HasAbstractRoute> longestPrefixMatch(Ip address) {
    return _longestPrefixMatchResults.get(address);
  }

  @Override
  public Set<HasAbstractRoute> longestPrefixMatch(Ip address, int maxPrefixLength) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean mergeRoute(HasAbstractRoute route) {
    return _mergeRouteTrues.contains(route);
  }
}
