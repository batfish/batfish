package org.batfish.question.routes;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;

/** Mock rib that only supports one operation: returning pre-set routes. */
class MockRib<R extends AbstractRoute> implements GenericRib<R> {

  private static final long serialVersionUID = 1L;

  private Set<R> _routes;

  MockRib() {
    _routes = ImmutableSet.of();
  }

  MockRib(Set<R> routes) {
    _routes = routes;
  }

  @Override
  public int comparePreference(R lhs, R rhs) {
    return 0;
  }

  @Override
  public Map<Prefix, IpSpace> getMatchingIps() {
    throw new UnsupportedOperationException();
  }

  @Override
  public SortedSet<Prefix> getPrefixes() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IpSpace getRoutableIps() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<R> getRoutes() {
    return _routes;
  }

  @Override
  public Set<R> longestPrefixMatch(Ip address) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean mergeRoute(R route) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<Prefix, Set<Ip>> nextHopIpsByPrefix() {
    throw new UnsupportedOperationException();
  }
}
