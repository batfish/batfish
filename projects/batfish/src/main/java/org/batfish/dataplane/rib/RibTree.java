package org.batfish.dataplane.rib;

import static org.batfish.dataplane.rib.RouteAdvertisement.Reason.REPLACE;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixTrieMultiMap;
import org.batfish.dataplane.rib.RibDelta.Builder;
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

  private static final long serialVersionUID = 1L;

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
    boolean removed = _root.remove(route.getNetwork(), route);
    if (!removed) {
      return RibDelta.empty();
    }

    Builder<R> b = RibDelta.builder();
    b.remove(route, reason);
    if (_root.get(route.getNetwork()).isEmpty() && _owner._backupRoutes != null) {
      Set<? extends R> backups =
          _owner._backupRoutes.getOrDefault(route.getNetwork(), ImmutableSortedSet.of());
      if (backups.isEmpty()) {
        return b.build();
      }
      // re-merge any backups we have
      backups.forEach(r -> b.from(mergeRoute(r)));
    }
    // Return new delta
    return b.build();
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
      return RibDelta.<R>builder().add(route).build();
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
        return RibDelta.<R>builder().add(route).build();
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
      return RibDelta.<R>builder().remove(routes, REPLACE).add(route).build();
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
    return (obj == this) || (obj instanceof RibTree && this._root.equals(((RibTree<?>) obj)._root));
  }
}
