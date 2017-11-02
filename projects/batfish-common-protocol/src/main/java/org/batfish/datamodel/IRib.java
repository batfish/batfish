package org.batfish.datamodel;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.batfish.datamodel.collections.MultiSet;

public interface IRib<R extends AbstractRoute> extends Serializable {

  /**
   * Compare the preferability of one route with anther
   *
   * @param lhs 1st route with which to compare preference
   * @param rhs 2nd route with which to compare preference
   * @return -1 if lhs route is less preferable than rhs; 0 if lhs route and rhs are equally
   *     preferable (i.e. for multipath routing); 1 if lhs route is strictly more preferred than rhs
   */
  int comparePreference(R lhs, R rhs);

  MultiSet<Prefix> getPrefixCount();

  SortedSet<Prefix> getPrefixes();

  /** Return a set of routes this RIB contains. */
  Set<R> getRoutes();

  Map<Integer, Map<Ip, List<AbstractRoute>>> getRoutesByPrefixPopularity();

  Set<R> longestPrefixMatch(Ip address);

  boolean mergeRoute(R route);

  Map<Prefix, Set<Ip>> nextHopIpsByPrefix();
}
