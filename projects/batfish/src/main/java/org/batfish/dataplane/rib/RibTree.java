package org.batfish.dataplane.rib;

import static org.batfish.common.util.CollectionUtil.maxValues;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixTrieMultiMap;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;

/**
 * Used to store the routes, supports longest prefix match operation.
 *
 * <p>This data structure is a more restrictive version of a ddNF (disjoint difference Normal Form),
 * where the wildcard symbols can appear only after (to-the-right-of) non wildcard symbols in the
 * bit vector. E.g., 101010**, but not 1*001***
 */
@ParametersAreNonnullByDefault
final class RibTree<R extends AbstractRouteDecorator> implements Serializable {

  @Nonnull private final PrefixTrieMultiMap<R> _root;
  @Nonnull private final AbstractRib<R> _owner;

  RibTree(AbstractRib<R> owner) {
    _root = new PrefixTrieMultiMap<>(Prefix.ZERO);
    _owner = owner;
  }

  /**
   * Remove a single route from the RIB, if it exists
   *
   * @param route route to remove
   * @return {@link RibDelta} if the route was removed, otherwise {@code null};
   */
  @Nonnull
  RibDelta<R> removeRouteGetDelta(R route, Reason reason) {
    assert reason != Reason.ADD : "cannot remove a route with reason ADD";

    Prefix network = route.getNetwork();
    boolean removed = _root.remove(network, route);
    if (!removed) {
      return RibDelta.empty();
    }

    RouteAdvertisement<R> removeRoute = new RouteAdvertisement<>(route, reason);

    if (!_root.get(network).isEmpty()) {
      // we still have a route for the network, so don't need to re-merge backups
      return RibDelta.of(removeRoute);
    }

    @Nullable
    Set<R> backups = _owner._backupRoutes == null ? null : _owner._backupRoutes.get(network);
    if (backups == null || backups.isEmpty()) {
      // no backup routes
      return RibDelta.of(removeRoute);
    }

    // find the best backup route(s) to add
    Collection<R> bestBackups = maxValues(backups, _owner::comparePreference);
    _root.putAll(network, bestBackups);

    return RibDelta.of(
        Streams.concat(Stream.of(removeRoute), bestBackups.stream().map(RouteAdvertisement::adding))
            .collect(ImmutableList.toImmutableList()));
  }

  /** Remove all routes from the tree */
  public void clear() {
    _root.clear();
  }

  /**
   * Check if the route is present in the RIB
   *
   * @param route route to find
   * @return true if the route exists in the RIB
   */
  boolean containsRoute(R route) {
    return _root.get(route.getNetwork()).contains(route);
  }

  private boolean hasForwardingRoute(Set<R> routes) {
    return routes.stream().anyMatch(r -> !r.getAbstractRoute().getNonForwarding());
  }

  private boolean onlyForwardingRoutes(Set<R> routes) {
    return routes.stream().noneMatch(r -> r.getAbstractRoute().getNonForwarding());
  }

  /**
   * Returns a set of routes in this tree which 1) are forwarding routes, 2) match the given IP
   * address, and 3) have the longest prefix length within the specified maximum.
   *
   * <p>Returns the empty set if there are no forwarding routes that match.
   */
  @Nonnull
  Set<R> getLongestPrefixMatch(Ip address, int maxPrefixLength) {
    for (int pl = maxPrefixLength; pl >= 0; pl--) {
      Set<R> routes = _root.longestPrefixMatch(address, pl);
      if (hasForwardingRoute(routes)) {
        if (onlyForwardingRoutes(routes)) {
          return routes;
        }
        return routes.stream()
            .filter(r -> !r.getAbstractRoute().getNonForwarding())
            .collect(ImmutableSet.toImmutableSet());
      }
    }
    return ImmutableSet.of();
  }

  /**
   * Return a set of all routes contained in this RIB
   *
   * @return a {@link Set} of routes
   */
  public Set<R> getRoutes() {
    return _root.getAllElements();
  }

  /** Retrieve stored routes for a particular prefix only. */
  public Set<R> getRoutes(Prefix prefix) {
    return _root.get(prefix);
  }

  /**
   * Add a new route into the RIB, potentially replacing other routes
   *
   * @param route route to add
   * @return a {@link RibDelta} objects indicating which routes where added and evicted from this
   *     RIB
   */
  @Nonnull
  RibDelta<R> mergeRoute(R route) {
    Set<R> routes = _root.get(route.getNetwork());
    if (routes.isEmpty()) {
      _root.put(route.getNetwork(), route);
      return RibDelta.adding(route);
    }
    /*
     * Check if the route we are adding is preferred to the routes we already have.
     * We only need to compare to one route, because all routes already in this node have the
     * same preference level. Hence, the route we are checking will be better than all,
     * worse than all, or at the same preference level.
     */

    R oldRoute = routes.iterator().next();
    int preferenceComparison = _owner.comparePreference(route, oldRoute);
    if (preferenceComparison < 0) { // less preferable, so route doesn't get added
      return RibDelta.empty();
    }
    if (preferenceComparison == 0) { // equal preference, so add for multipath routing
      // Otherwise add the route
      if (_root.put(route.getNetwork(), route)) {
        return RibDelta.adding(route);
      } else {
        return RibDelta.empty();
      }
    }
    /*
     * Last case, preferenceComparison > 0
     * Better than all existing routes for this prefix, so
     * replace them with this one.
     */
    if (_root.replaceAll(route.getNetwork(), route)) {
      // build the RibDelta directly, since we know the routes are distinct
      List<RouteAdvertisement<R>> actions =
          Streams.concat(
                  routes.stream().map(RouteAdvertisement::replacing),
                  Stream.of(RouteAdvertisement.adding(route)))
              .collect(ImmutableList.toImmutableList());
      assert actions.stream().map(RouteAdvertisement::getRoute).distinct().count() == actions.size()
          : "replaced routes and added route should be distinct";
      return RibDelta.of(actions);
    } else {
      return RibDelta.empty();
    }
  }

  @Override
  public int hashCode() {
    return _root.hashCode();
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return (obj == this) || (obj instanceof RibTree && _root.equals(((RibTree<?>) obj)._root));
  }
}
